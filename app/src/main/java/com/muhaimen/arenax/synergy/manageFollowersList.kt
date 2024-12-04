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

class manageFollowersList : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var followersAdapter: FollowersAdapter
    private lateinit var database: DatabaseReference
    private lateinit var currentUserId: String
    private val followersList = mutableListOf<UserData>()

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
        followersAdapter = FollowersAdapter(
            profiles = followersList,
            onMessageClick = { follower -> fetchUserChats(follower.userId) },
            onRemoveClick = { follower -> showRemoveFollowerDialog(follower) }
        )

        // Set the adapter to the RecyclerView
        recyclerView.adapter = followersAdapter

        // Fetch followers from Firebase
        fetchFollowers()

        return binding
    }

    private fun fetchFollowers() {
        // Get the reference for the followers node
        val followersRef = database.child("userData").child(currentUserId).child("synerG").child("followers")

        // Query for followers where the status is "accepted"
        followersRef.orderByChild("status").equalTo("accepted").get().addOnSuccessListener { snapshot ->
            // Log the snapshot response to see what is returned
            Log.d("ManageFollowers", "fetchFollowers success: ${snapshot.value}")

            // Check if the snapshot has data
            if (snapshot.exists()) {
                // List to store the followers' Firebase UIDs
                val followerUids = mutableListOf<String>()

                // Loop through the follower ids and add to the list
                for (followerSnapshot in snapshot.children) {
                    val followerId = followerSnapshot.key
                    if (followerId != null) {
                        // Log each follower ID being processed
                        Log.d("ManageFollowers", "Adding follower ID: $followerId")
                        followerUids.add(followerId)
                    }
                }

                // Send the list of follower IDs to the backend server
                fetchFollowersDataFromBackend(followerUids)
            } else {
                Toast.makeText(requireContext(), "No accepted followers found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Log the error message for debugging
            Log.e("ManageFollowers", "Error fetching followers: ${exception.message}")
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchFollowersDataFromBackend(followerUids: List<String>) {
        val url = "${Constants.SERVER_URL}exploreAccounts/fetchFollowersData"
        val jsonBody = JSONObject().apply {
            put("followerUids", JSONArray(followerUids))
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    // Handle response (e.g., update UI with the follower data)
                    val responseData = response.body?.string()
                    Log.d("ManageFollowers", "Received follower data: $responseData")

                    // Parse the JSON response to extract follower data
                    if (!responseData.isNullOrEmpty()) {
                        try {
                            val jsonArray = JSONArray(responseData)
                            val followersList = mutableListOf<UserData>()

                            for (i in 0 until jsonArray.length()) {
                                val followerJson = jsonArray.getJSONObject(i)

                                val firebaseUid = followerJson.getString("firebaseUid")
                                val fullName = followerJson.getString("fullName")
                                val gamerTag = followerJson.getString("gamerTag")
                                val gamerRank = followerJson.getInt("gamerRank") // Rank could be 0 for unranked users
                                val profilePictureUrl = followerJson.getString("profilePictureUrl")

                                // Handle unranked users (rank = 0)
                                val rankDisplay = when {
                                    gamerRank == 0 -> {
                                        "Unranked"  // Display a custom message for unranked users
                                    }
                                    else -> {
                                        "$gamerRank"  // Display the actual rank
                                    }
                                }

                                // Create UserData object for each follower
                                val follower = UserData(
                                    userId = firebaseUid,
                                    fullname = fullName,
                                    gamerTag = gamerTag,
                                    rank = rankDisplay,  // Store the rank display string
                                    profilePicture = profilePictureUrl
                                )

                                // Add to the list
                                followersList.add(follower)
                            }

                            // Update the adapter or UI with the list
                            withContext(Dispatchers.Main) {
                                // Assuming followersAdapter is accessible and initialized
                                followersAdapter.updateProfiles(followersList)
                            }

                        } catch (e: JSONException) {
                            Log.e("ManageFollowers", "Error parsing follower data: ${e.message}")
                        }
                    }
                } else {
                    Log.e("ManageFollowers", "Error sending data to backend: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("ManageFollowers", "Error sending data to backend: ${e.message}")
            }
        }
    }




    private fun showRemoveFollowerDialog(follower: UserData) {
        // Show an AlertDialog to confirm the removal of the follower
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Remove Follower")
        dialogBuilder.setMessage("Do you want to remove ${follower.fullname} as a follower?")

        dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
            removeFollower(follower)
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        dialogBuilder.create().show()
    }

    private fun removeFollower(follower: UserData) {
        // Remove the follower from the current user's followers list
        val currentUserFollowersRef = database.child("userData").child(currentUserId).child("synerG").child("followers")
        currentUserFollowersRef.child(follower.userId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Log success
                Log.d("ManageFollowers", "Successfully removed follower: ${follower.userId}")

                // Remove the current user from the follower's following list
                val followerFollowingRef = database.child("userData").child(follower.userId).child("synerG").child("following")
                followerFollowingRef.child(currentUserId).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "Follower removed successfully.", Toast.LENGTH_SHORT).show()

                        // Update the followers list locally
                        followersList.remove(follower)
                        followersAdapter.notifyDataSetChanged()
                    } else {
                        Log.e("ManageFollowers", "Error removing follower from following list: ${it.exception?.message}")
                        Toast.makeText(requireContext(), "Error removing follower.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Log failure
                Log.e("ManageFollowers", "Error removing follower: ${task.exception?.message}")
                Toast.makeText(requireContext(), "Error removing follower.", Toast.LENGTH_SHORT).show()
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
                // Start the ChatActivity with the data
                startActivity(intent)
            } else {
                // If data doesn't exist in Firebase, log the failure
                Log.d("ChatActivity", "No data found for receiverId: $receiverId. Defaulting to Unknown User data.")

                // Handle failure to retrieve data and start the chat with default values
                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra("userId", receiverId)
                    putExtra("fullname", "Unknown User")
                    putExtra("gamerTag", "Unknown GamerTag")
                    putExtra("profilePicture", "null") // or a placeholder image URL
                    putExtra("gamerRank", "00") // Default value
                }
                startActivity(intent)
            }
        }.addOnFailureListener { exception ->
            // Log failure with exception message
            Log.e("ChatActivity", "Error retrieving user data for $receiverId: ${exception.message}")
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
