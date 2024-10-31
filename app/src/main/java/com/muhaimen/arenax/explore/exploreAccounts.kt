package com.muhaimen.arenax.explore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserProfile
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class exploreAccounts : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: exploreAccountsAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_accounts, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.accounts_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch user profiles
        fetchUserProfiles()

        return view
    }

    private fun fetchUserProfiles() {
        // Replace with your backend URL and userId
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        val url = "${Constants.SERVER_URL}api/user/$userId/usersList"

        val queue = Volley.newRequestQueue(requireContext())
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val profiles = parseUserProfiles(response)
                // Set up the adapter with fetched data
                adapter = exploreAccountsAdapter(profiles)
                recyclerView.adapter = adapter
            },
            { error ->
                Log.e("Volley Error", "Error fetching user profiles: ${error.message}")
            }
        )

        queue.add(jsonArrayRequest)
    }

    private fun parseUserProfiles(jsonArray: JSONArray): List<UserProfile> {
        val profiles = mutableListOf<UserProfile>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
            val fullName = jsonObject.getString("fullName")
            val gamerTag = jsonObject.getString("gamerTag")
            val gamerRank = jsonObject.getString("gamerRank")
            val profilePictureUrl = jsonObject.getString("profilePictureUrl")

            val userProfile = UserProfile(fullName, gamerTag, gamerRank, profilePictureUrl)
            profiles.add(userProfile)
        }

        return profiles
    }
}
