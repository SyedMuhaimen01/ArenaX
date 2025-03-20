package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageFollowing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.inbox.Threads.organizationChatActivity
import com.muhaimen.arenax.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class organizationFollowersList : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var followersAdapter: organizationFollowersAdapter
    private val followersList = mutableListOf<UserData>()
    private lateinit var orgName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organization_followers_list, container, false)
        recyclerView = view.findViewById(R.id.followersList_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        orgName = requireArguments().getString("organization_name").toString()

        followersAdapter = organizationFollowersAdapter(
            profiles = followersList,
            onMessageClick = { follower -> getOrganizationDetails("fetchUserChats", follower, null) },
            onRemoveClick = { follower -> showRemoveFollowerDialog(follower) }
        )
        recyclerView.adapter = followersAdapter

        getOrganizationDetails("initializeOrgId", null, null)

        return view
    }

    private fun fetchFollowers(orgId: String) {
        val followersRef = FirebaseDatabase.getInstance().getReference("organizationsData").child(orgId).child("synerG").child("followers")

        followersRef.orderByChild("status").equalTo("accepted").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val followerUids = snapshot.children.mapNotNull { it.key }
                fetchFollowersDataFromBackend(followerUids)
            } else {
                Toast.makeText(requireContext(), "No followers found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("ManageFollowers", "Error fetching followers: ${exception.message}")
        }
    }

    private fun fetchFollowersDataFromBackend(followerUids: List<String>) {
        val url = "${Constants.SERVER_URL}exploreAccounts/fetchFollowersData"
        val jsonBody = JSONObject().apply { put("followerUids", JSONArray(followerUids)) }
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder().url(url).post(requestBody).build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = OkHttpClient().newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseData ->
                        Log.d("ManageFollowers", "Received follower data: $responseData")
                        val jsonArray = JSONArray(responseData)
                        val updatedFollowersList = mutableListOf<UserData>()

                        for (i in 0 until jsonArray.length()) {
                            val followerJson = jsonArray.getJSONObject(i)
                            val follower = UserData(
                                userId = followerJson.getString("firebaseUid"),
                                fullname = followerJson.getString("fullName"),
                                gamerTag = followerJson.getString("gamerTag"),
                                rank = followerJson.optInt("gamerRank", 0).toString(),
                                profilePicture = followerJson.getString("profilePictureUrl")
                            )
                            updatedFollowersList.add(follower)
                        }
                        withContext(Dispatchers.Main) { followersAdapter.updateProfiles(updatedFollowersList) }
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
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Follower")
            .setMessage("Do you want to remove ${follower.fullname} as a follower?")
            .setPositiveButton("Yes") { dialog, _ ->
                getOrganizationDetails("removeFollower", follower, null)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    private fun removeFollower(follower: UserData, orgId: String) {
        val orgFollowersRef = FirebaseDatabase.getInstance().getReference("organizationsData").child(orgId).child("synerG").child("followers")
        orgFollowersRef.child(follower.userId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                FirebaseDatabase.getInstance().getReference("userData").child(follower.userId).child("synerG").child("following")
                    .child(orgId).removeValue().addOnCompleteListener {
                        if (it.isSuccessful) {
                            followersList.remove(follower)
                            followersAdapter.notifyDataSetChanged()
                        }
                    }
            }
        }
    }

    private fun fetchUserChats(receiverId: String, orgId: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("userData").child(receiverId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val intent = Intent(requireContext(), organizationChatActivity::class.java).apply {
                putExtra("userId", receiverId)
                putExtra("fullname", dataSnapshot.child("fullname").value.toString())
                putExtra("gamerTag", dataSnapshot.child("gamerTag").value.toString())
                putExtra("profilePicture", dataSnapshot.child("profilePicture").value.toString())
                putExtra("gamerRank", dataSnapshot.child("gamerRank").value.toString())
                putExtra("organizationId", orgId)
            }
            startActivity(intent)
        }
    }

    private fun getOrganizationDetails(calledBy: String, follower: UserData?, followerId: String?) {
        FirebaseDatabase.getInstance().getReference("organizationsData").orderByChild("organizationName").equalTo(orgName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.firstOrNull()?.key?.let { orgId ->
                        when (calledBy) {
                            "initializeOrgId" -> fetchFollowers(orgId)
                            "removeFollower" -> removeFollower(follower!!, orgId)
                            "fetchUserChats" -> fetchUserChats(followerId!!, orgId)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
