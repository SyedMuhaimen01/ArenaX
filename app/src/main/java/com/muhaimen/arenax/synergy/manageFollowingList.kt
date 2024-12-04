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
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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
        val followingRef = database.child("userData").child(currentUserId).child("synerG").child("following")
        followingRef.orderByChild("status").equalTo("accepted").get().addOnSuccessListener { snapshot ->

            if (snapshot.exists()) {
                // List to store the following users' Firebase UIDs
                val followingUids = mutableListOf<String>()

                // Loop through the following ids and add to the list
                for (followedSnapshot in snapshot.children) {
                    val followedId = followedSnapshot.key
                    if (followedId != null) {
                        followingUids.add(followedId)
                    } else {
                        Log.d("ManageFollowing", "Followed ID is null for a record")
                    }
                }

                if (followingUids.isNotEmpty()) {
                    // Send the list of following IDs to the backend server to fetch detailed user data
                    fetchFollowingDataFromBackend(followingUids)
                } else {
                    Toast.makeText(requireContext(), "You are not following anyone", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "You are not following anyone", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Log the error message for debugging
            Log.e("ManageFollowing", "Error fetching following list: ${exception.message}")
        }
    }


    // Function to fetch detailed following data from the backend
    // Function to fetch detailed following data from the backend
    private fun fetchFollowingDataFromBackend(followingUids: List<String>) {
        // Log the list of following UIDs being sent to the backend
        Log.d("ManageFollowing", "Sending following UIDs to backend: $followingUids")

        val url = "${Constants.SERVER_URL}exploreAccounts/fetchFollowingData"
        val jsonBody = JSONObject().apply {
            put("followingUids", JSONArray(followingUids))
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d("ManageFollowing", "Sending request to backend URL: $url")
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    // Handle response (e.g., update UI with the following data)
                    val responseData = response.body?.string()
                    Log.d("ManageFollowing", "Received following data: $responseData")

                    // Parse the JSON response to extract following data
                    if (!responseData.isNullOrEmpty()) {
                        try {
                            val jsonArray = JSONArray(responseData)
                            val followingList = mutableListOf<UserData>()

                            for (i in 0 until jsonArray.length()) {
                                val followedJson = jsonArray.getJSONObject(i)

                                val firebaseUid = followedJson.getString("firebaseUid")
                                val fullName = followedJson.getString("fullName")
                                val gamerTag = followedJson.getString("gamerTag")
                                val gamerRank = followedJson.getInt("gamerRank") // Rank could be 0 for unranked users
                                val profilePictureUrl = followedJson.getString("profilePictureUrl")

                                // Handle unranked users (rank = 0)
                                val rankDisplay = when {
                                    gamerRank == 0 -> {
                                        "Unranked"  // Display a custom message for unranked users
                                    }
                                    else -> {
                                        "$gamerRank"  // Display the actual rank
                                    }
                                }

                                // Create UserData object for each followed user
                                val followedUser = UserData(
                                    userId = firebaseUid,
                                    fullname = fullName,
                                    gamerTag = gamerTag,
                                    rank = rankDisplay,  // Store the rank display string
                                    profilePicture = profilePictureUrl
                                )

                                // Add to the list
                                followingList.add(followedUser)
                            }

                            // Log the size of the list before updating the UI
                            Log.d("ManageFollowing", "Number of following users fetched: ${followingList.size}")

                            // Update the adapter or UI with the list
                            withContext(Dispatchers.Main) {
                                // Assuming followingAdapter is accessible and initialized
                                followingAdapter.updateProfiles(followingList)
                            }

                        } catch (e: JSONException) {
                            Log.e("ManageFollowing", "Error parsing following data: ${e.message}")
                        }
                    } else {
                        Log.d("ManageFollowing", "Received empty or null data from the backend.")
                    }
                } else {
                    Log.e("ManageFollowing", "Error sending data to backend: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("ManageFollowing", "Error sending data to backend: ${e.message}")
            }
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
