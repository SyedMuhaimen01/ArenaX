package com.muhaimen.arenax.explore

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class exploreAccounts : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: exploreAccountsAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    @SuppressLint("MissingInflatedId")
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

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primaryColor)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            fetchUserProfiles()
        }

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

    private fun parseUserProfiles(jsonArray: JSONArray): List<UserData> {
        val profiles = mutableListOf<UserData>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)

            // Parsing data as per the backend route response
            val firebaseUid = jsonObject.optString("firebaseUid", "")
            val fullName = jsonObject.optString("fullName", "")
            val gamerTag = jsonObject.optString("gamerTag", "")
            val profilePicture = jsonObject.optString("profilePictureUrl", null)
            val gamerRank = jsonObject.optInt("gamerRank", 0) // Assuming rank is an integer

            val userData = UserData(
                userId = firebaseUid,             // Mapping firebaseUid to userId in UserData
                fullname = fullName,
                email = "",                       // Not available in the response
                dOB = "",                         // Not available in the response
                gamerTag = gamerTag,
                profilePicture = profilePicture,
                gender = Gender.PreferNotToSay,   // Assuming default as not available in response
                bio = null,                       // Not available in the response
                location = null,                  // Not available in the response
                accountVerified = false,         // Not available in the response
                rank = gamerRank
            )

            profiles.add(userData)
            swipeRefreshLayout.isRefreshing = false
        }

        return profiles
    }


}
