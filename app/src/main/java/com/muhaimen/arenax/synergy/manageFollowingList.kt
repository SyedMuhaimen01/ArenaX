package com.muhaimen.arenax.synergy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.ChatActivity
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager

class manageFollowingList : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var followingAdapter: FollowingAdapter
    private lateinit var database: DatabaseReference
    private lateinit var currentUserId: String
    private val followingList = mutableListOf<UserData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment layout
        val binding = inflater.inflate(R.layout.fragment_manage_followers_list, container, false)

        // Initialize RecyclerView
        recyclerView = binding.findViewById(R.id.followersList_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Get the current user ID (assuming it's stored in shared preferences or passed through arguments)
        currentUserId = FirebaseManager.getCurrentUserId().toString()

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Initialize the adapter
        followingAdapter = FollowingAdapter(
            profiles = followingList,
            onMessageClick = { followedUser -> fetchUserChats(followedUser.userId) },
            onRemoveClick = { followedUser -> showRemoveFollowingDialog(followedUser) }
        )

        // Set the adapter to the RecyclerView
        recyclerView.adapter = followingAdapter

        // Fetch the users the current user is following
        fetchFollowing()

        return binding
    }

    private fun fetchFollowing() {
        // Get the reference for the following node
        val followingRef = database.child("userData").child(currentUserId).child("synerG").child("following")

        // Query for the users the current user is following
        followingRef.orderByChild("status").equalTo("accepted").get().addOnSuccessListener { snapshot ->
            // Log the snapshot response to see what is returned
            Log.d("ManageFollowing", "fetchFollowing success: ${snapshot.value}")

            // Check if the snapshot has data
            if (snapshot.exists()) {
                // Loop through the following ids and fetch their details
                for (followedSnapshot in snapshot.children) {
                    val followedId = followedSnapshot.key

                    if (followedId != null) {
                        // Log each followed ID being processed
                        Log.d("ManageFollowing", "Fetching data for followed ID: $followedId")
                        // Get followed user details using their ID
                        fetchFollowedUserData(followedId)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "You are not following anyone", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Log the error message for debugging
            Log.e("ManageFollowing", "Error fetching following list: ${exception.message}")
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchFollowedUserData(followedId: String) {
        // Fetch the followed user's data from the database
        val followedRef = database.child("userData").child(followedId)
        followedRef.get().addOnSuccessListener { snapshot ->
            // Log the response to see the data retrieved for each followed user
            Log.d("ManageFollowing", "fetchFollowedUserData success for $followedId: ${snapshot.value}")

            if (snapshot.exists()) {
                val fullName = snapshot.child("fullname").value.toString()
                val gamerTag = snapshot.child("gamerTag").value.toString()
                val gamerRank = snapshot.child("gamerRank").value.toString()
                val profilePicture = snapshot.child("profilePicture").value.toString()

                // Create a UserData object and add it to the followingList
                val followedUser = UserData(
                    profilePicture = profilePicture,
                    fullname = fullName,
                    gamerTag = gamerTag,
                    rank = gamerRank.toIntOrNull() ?: 0 // Ensure rank is properly parsed
                )

                // Store the followed user's ID for later removal
                followedUser.userId = followedId // Assuming you have this field in UserData

                followingList.add(followedUser)

                // Notify the adapter that the data has changed
                followingAdapter.notifyDataSetChanged()
            }
        }.addOnFailureListener { exception ->
            // Log the error message for debugging
            Log.e("ManageFollowing", "Error fetching followed user data for $followedId: ${exception.message}")
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRemoveFollowingDialog(followedUser: UserData) {
        // Show an AlertDialog to confirm the removal of the followed user
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Unfollow User")
        dialogBuilder.setMessage("Do you want to unfollow ${followedUser.fullname}?")

        dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
            removeFollowing(followedUser)
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        dialogBuilder.create().show()
    }

    private fun removeFollowing(followedUser: UserData) {
        // Remove the followed user from the current user's following list
        val currentUserFollowingRef = database.child("userData").child(currentUserId).child("synerG").child("following")
        currentUserFollowingRef.child(followedUser.userId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Log success
                Log.d("ManageFollowing", "Successfully unfollowed user: ${followedUser.userId}")

                // Remove the current user from the followed user's followers list (if applicable)
                val followedUserFollowersRef = database.child("userData").child(followedUser.userId).child("synerG").child("followers")
                followedUserFollowersRef.child(currentUserId).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "User unfollowed successfully.", Toast.LENGTH_SHORT).show()

                        // Update the following list locally
                        followingList.remove(followedUser)
                        followingAdapter.notifyDataSetChanged()
                    } else {
                        Log.e("ManageFollowing", "Error removing user from followers list: ${it.exception?.message}")
                        Toast.makeText(requireContext(), "Error unfollowing user.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Log failure
                Log.e("ManageFollowing", "Error unfollowing user: ${task.exception?.message}")
                Toast.makeText(requireContext(), "Error unfollowing user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserChats(receiverId: String) {
        // Log the receiverId to see what data is being passed
        Log.d("ChatActivity", "fetchUserChats called with receiverId: $receiverId")

        val database = FirebaseManager.getDatabseInstance()
        val userRef = database.getReference("userData").child(receiverId)

        // Try to fetch the user data from Firebase
        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                // Fetch only the necessary user data
                val profileImageUrl = dataSnapshot.child("profilePicture").value?.toString() ?: ""
                val fullname = dataSnapshot.child("fullname").value?.toString() ?: "Unknown User"
                val gamerTag = dataSnapshot.child("gamerTag").value?.toString() ?: "Unknown GamerTag"
                val gamerRank = dataSnapshot.child("gamerRank").value?.toString() ?: "00" // Adjust logic to fetch gamerRank if needed

                // Log the fetched data for debugging
                Log.d("ChatActivity", "Data retrieved for $receiverId: Fullname = $fullname, GamerTag = $gamerTag, ProfilePicture = $profileImageUrl, GamerRank = $gamerRank")

                // Create intent and pass user data
                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra("userId", receiverId)
                    putExtra("fullname", fullname)
                    putExtra("gamerTag", gamerTag)
                    putExtra("profilePicture", profileImageUrl)
                    putExtra("gamerRank", gamerRank)
                }

                // Start the ChatActivity
                startActivity(intent)
            }
        }
    }
}
