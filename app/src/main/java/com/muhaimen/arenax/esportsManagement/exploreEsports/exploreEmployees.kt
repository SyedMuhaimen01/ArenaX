package com.muhaimen.arenax.esportsManagement.exploreEsports

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class exploreEmployees : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: exploreEmployeesAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var searchUserRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private val currentUserId = FirebaseManager.getCurrentUserId()
    private lateinit var database: DatabaseReference
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_accounts, container, false)

        recyclerView = view.findViewById(R.id.accounts_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.searchbar)
        searchUserRecyclerView = view.findViewById(R.id.searchUserRecyclerView)
        val searchAdapter = SearchAdapter(emptyList())
        searchUserRecyclerView.layoutManager = LinearLayoutManager(context)
        searchUserRecyclerView.adapter = searchAdapter
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                recyclerView.visibility = View.GONE
                searchUserRecyclerView.visibility = View.VISIBLE
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.trim() ?: ""
                if (searchText.isNotEmpty()) {
                    searchUsers(searchText, searchAdapter)
                } else {
                    searchAdapter.updateUserList(emptyList())
                }
            }
        })

        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchUserRecyclerView.visibility == View.VISIBLE) {
                    searchUserRecyclerView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    searchEditText.text.clear()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })

        // Initialize database reference
        database = FirebaseDatabase.getInstance().getReference("userData")
        // Fetch user profiles
        fetchExploreUsers()
        //fetchUserProfiles()

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primaryColor)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            //fetchUserProfiles()
            fetchExploreUsers()
        }

        return view
    }

    //Not required in current implementation
    private fun fetchUserProfiles() {
        // Replace with your backend URL and userId
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        val url = "${Constants.SERVER_URL}exploreAccounts/user/$userId/usersList"

        val queue = Volley.newRequestQueue(requireContext())
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val profiles = parseUserProfiles(response)
                // Set up the adapter with fetched data
                adapter = exploreEmployeesAdapter(profiles)
                recyclerView.adapter = adapter
            },
            { error ->
                Log.e("Volley Error", "Error fetching user profiles: ${error.message}")
            }
        )

        queue.add(jsonArrayRequest)
    }
    //Not required in current implementation
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
                rank = gamerRank.toString()
            )

            profiles.add(userData)
            swipeRefreshLayout.isRefreshing = false
        }

        return profiles
    }

    private fun searchUsers(query: String, adapter: SearchAdapter) {
        val usersList = mutableListOf<UserData>()
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserData::class.java)
                    if (user != null && user.userId != currentUserId) {
                        // Perform a case-insensitive check on fullname and gamertag
                        if (user.fullname.contains(query, ignoreCase = true) ||
                            user.gamerTag.contains(query, ignoreCase = true)) {
                            // Add only unique users to the list
                            if (!usersList.any { it.userId == user.userId }) {
                                usersList.add(user)
                            }
                        }
                    }
                }
                adapter.updateUserList(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ExploreAccounts", "Search error: ${error.message}")
                Toast.makeText(context, "Error searching users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchExploreUsers() {
        auth = FirebaseAuth.getInstance()
        val firebaseUid = auth.currentUser?.uid
        val url = "${Constants.SERVER_URL}exploreAccounts/user/${firebaseUid}/fetchAccounts"
        val queue = Volley.newRequestQueue(requireContext())
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val profiles = parseExploreProfiles(response)
                    adapter = exploreEmployeesAdapter(profiles)
                    recyclerView.adapter = adapter
                } catch (e: Exception) {
                    Log.e("JSON Parsing Error", "Error parsing response: ${e.message}")
                }
            },
            { error ->
                Log.e("Volley Error", "Error fetching user profiles: ${error.message}")
            }
        )
        queue.add(jsonArrayRequest)
    }

    private fun parseExploreProfiles(jsonArray: JSONArray): List<UserData> {
        val profiles = mutableListOf<UserData>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
            val firebaseUid = jsonObject.optString("firebaseUid", "")
            val fullName = jsonObject.optString("fullName", "")
            val gamerTag = jsonObject.optString("gamerTag", "")
            val profilePicture = jsonObject.optString("profilePictureUrl", null)
            val gamerRank = jsonObject.optString("gamerRank", "Unranked") // Default value for rank
            val similarity = jsonObject.optDouble("similarity", 0.0)

            val userData = UserData(
                userId = firebaseUid,
                fullname = fullName,
                email = "",
                dOB = "",
                gamerTag = gamerTag,
                profilePicture = profilePicture,
                gender = Gender.PreferNotToSay,
                bio = null,
                location = null,
                accountVerified = false,
                rank = gamerRank,
            )
            profiles.add(userData)
        }
        return profiles
    }
}
