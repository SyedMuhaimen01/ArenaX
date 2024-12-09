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
        val binding = inflater.inflate(R.layout.fragment_manage_followers_list, container, false)
        recyclerView = binding.findViewById(R.id.followersList_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        database = FirebaseDatabase.getInstance().reference
        currentUserId = FirebaseManager.getCurrentUserId().toString()

        followersAdapter = FollowersAdapter(
            profiles = followersList,
            onMessageClick = { follower -> fetchUserChats(follower.userId) },
            onRemoveClick = { follower -> showRemoveFollowerDialog(follower) }
        )
        recyclerView.adapter = followersAdapter
        fetchFollowers()
        return binding
    }

    private fun fetchFollowers() {
        // Get the reference for the followers node
        val followersRef = database.child("userData").child(currentUserId).child("synerG").child("followers")

        // Query for followers where the status is "accepted"
        followersRef.orderByChild("status").equalTo("accepted").get().addOnSuccessListener { snapshot ->

            // Check if the snapshot has data
            if (snapshot.exists()) {
                // List to store the followers' Firebase UIDs
                val followerUids = mutableListOf<String>()

                // Loop through the follower ids and add to the list
                for (followerSnapshot in snapshot.children) {
                    val followerId = followerSnapshot.key
                    if (followerId != null) {
                        followerUids.add(followerId)
                    }
                }
                fetchFollowersDataFromBackend(followerUids)
            } else {
                Toast.makeText(requireContext(), "No followers found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Log the error message for debugging
            Log.e("ManageFollowers", "Error fetching followers: ${exception.message}")
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
                    val responseData = response.body?.string()
                    Log.d("ManageFollowers", "Received follower data: $responseData")
                    if (!responseData.isNullOrEmpty()) {
                        try {
                            val jsonArray = JSONArray(responseData)
                            val followersList = mutableListOf<UserData>()

                            for (i in 0 until jsonArray.length()) {
                                val followerJson = jsonArray.getJSONObject(i)

                                val firebaseUid = followerJson.getString("firebaseUid")
                                val fullName = followerJson.getString("fullName")
                                val gamerTag = followerJson.getString("gamerTag")
                                val gamerRank = followerJson.getInt("gamerRank")
                                val profilePictureUrl = followerJson.getString("profilePictureUrl")

                                val rankDisplay = when {
                                    gamerRank == 0 -> {
                                        "Unranked"
                                    }
                                    else -> {
                                        "$gamerRank"
                                    }
                                }

                                // UserData object for each follower
                                val follower = UserData(
                                    userId = firebaseUid,
                                    fullname = fullName,
                                    gamerTag = gamerTag,
                                    rank = rankDisplay,
                                    profilePicture = profilePictureUrl
                                )

                                followersList.add(follower)
                            }
                            withContext(Dispatchers.Main) {
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

    //Dialog box to confirm removing a follower
    private fun showRemoveFollowerDialog(follower: UserData) {
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
                    }
                }
            } else { }
        }
    }

    private fun fetchUserChats(receiverId: String) {
        val database = FirebaseManager.getDatabseInstance()
        val userRef = database.getReference("userData").child(receiverId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val profileImageUrl = dataSnapshot.child("profilePicture").value?.toString() ?: ""
                val fullname = dataSnapshot.child("fullname").value?.toString() ?: "Unknown User"
                val gamerTag = dataSnapshot.child("gamerTag").value?.toString() ?: "Unknown GamerTag"
                val gamerRank = dataSnapshot.child("gamerRank").value?.toString() ?: "0"

                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra("userId", receiverId)
                    putExtra("fullname", fullname)
                    putExtra("gamerTag", gamerTag)
                    putExtra("profilePicture", profileImageUrl)
                    putExtra("gamerRank", gamerRank)
                }

                startActivity(intent)
            } else { }
        }.addOnFailureListener { exception -> }
    }
}
