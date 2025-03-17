package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

class viewOwnTeam : AppCompatActivity() {
    private lateinit var teamLogoImageView:ImageView
    private lateinit var teamNameTextView:TextView
    private lateinit var gameNameTextView:TextView
    private lateinit var locationTextView:TextView
    private lateinit var taglineTextView:TextView
    private lateinit var teamCaptainTextView:TextView
    private lateinit var teamCaptainProfilePicture:ImageView
    private lateinit var teamDetailsTextView:TextView
    private lateinit var teamEmailTextView:TextView
    private lateinit var teamAchievementsTextView:TextView
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var searchUserRecyclerView: RecyclerView
    private lateinit var searchUserAdapter: SearchPlayerAdapter
    private lateinit var managePlayersAdapter: ManagePlayerAdapter
    private lateinit var addPlayerButton:FloatingActionButton
    private lateinit var searchBar: EditText
    private lateinit var database: DatabaseReference
    private lateinit var searchbarLinearLayout: LinearLayout
    private lateinit var organizationName: String
    private lateinit var teamName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_own_team)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        initializeUI()
        organizationName = intent.getStringExtra("organizationName") ?: ""
        teamName = intent.getStringExtra("teamName") ?: ""

        fetchTeamDetails()
        // Initialize Firebase Database Reference
        database = FirebaseDatabase.getInstance().reference.child("userData")

        searchUserAdapter = SearchPlayerAdapter(mutableListOf()) { userId ->
            addPlayer(userId)
        }

        managePlayersAdapter = ManagePlayerAdapter(mutableListOf()) { playerId ->
            removePlayer(playerId)
        }

        // Set Adapters
        playersRecyclerView.adapter = managePlayersAdapter
        searchUserRecyclerView.adapter = searchUserAdapter

        // Search Bar Focus Handling
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                playersRecyclerView.visibility = View.GONE
                searchUserRecyclerView.visibility = View.VISIBLE
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.trim() ?: ""
                if (searchText.isNotEmpty()) {
                    searchUsers(searchText)
                } else {
                    searchUserAdapter.updatePlayersList(emptyList())
                }
            }
        })

        // Floating Button Click Handling
        addPlayerButton.setOnClickListener {
            if (searchbarLinearLayout.visibility == View.VISIBLE) {
                searchbarLinearLayout.visibility = View.GONE
                searchUserRecyclerView.visibility = View.GONE
                playersRecyclerView.visibility = View.VISIBLE
            } else {
                searchbarLinearLayout.visibility = View.VISIBLE
                searchUserRecyclerView.visibility = View.VISIBLE
                playersRecyclerView.visibility = View.GONE
            }
        }


        // Handle system back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchUserRecyclerView.visibility == View.VISIBLE) {
                    searchUserRecyclerView.visibility = View.GONE
                    playersRecyclerView.visibility = View.VISIBLE
                    searchBar.text.clear()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        teamCaptainTextView.text?.toString()?.takeIf { it.isNotEmpty() }?.let { captainId ->
            database.child(captainId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(UserData::class.java)?.let { user ->
                        Glide.with(this@viewOwnTeam)
                            .load(user.profilePicture ?: R.drawable.battlegrounds_icon_background)
                            .placeholder(R.drawable.battlegrounds_icon_background)
                            .error(R.drawable.battlegrounds_icon_background)
                            .circleCrop()
                            .into(teamCaptainProfilePicture)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if necessary (e.g., log the error)
                }
            })
        }

    }

    fun initializeUI() {
        teamLogoImageView = findViewById(R.id.teamLogo)
        teamNameTextView = findViewById(R.id.teamNameTextView)
        gameNameTextView = findViewById(R.id.gameNameTextView)
        locationTextView = findViewById(R.id.teamLocationTextView)
        taglineTextView = findViewById(R.id.teamTaglineTextView)
        teamCaptainTextView = findViewById(R.id.captainNameTextView)
        teamCaptainProfilePicture = findViewById(R.id.teamCaptainProfilePicture)
        teamEmailTextView = findViewById(R.id.teamEmailTextView)
        teamAchievementsTextView = findViewById(R.id.team_achievements_TextView)
        teamDetailsTextView = findViewById(R.id.team_details_TextView)
        playersRecyclerView = findViewById(R.id.playersRecyclerView)
        searchUserRecyclerView = findViewById(R.id.searchUserRecyclerView)
        addPlayerButton = findViewById(R.id.addPlayerButton)
        searchBar = findViewById(R.id.searchbar)
        searchbarLinearLayout = findViewById(R.id.searchLinearLayout)
        playersRecyclerView.layoutManager = LinearLayoutManager(this)
        searchUserRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchTeamDetails() {
        val url = "${Constants.SERVER_URL}manageTeams/getTeamDetails"
        val requestBody = JSONObject().apply {
            put("teamName", teamName) // Ensure this matches the backend
        }

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.has("teamDetails")) {
                        val teamDetails = jsonResponse.getJSONObject("teamDetails")

                        // Extract team details from the response
                        val teamName = teamDetails.getString("teamName")
                        val gameName = teamDetails.getString("gameName")
                        val teamDetailsText = teamDetails.getString("teamDetails")
                        val teamLocation = teamDetails.getString("teamLocation")
                        val teamEmail = teamDetails.getString("teamEmail")
                        val teamCaptain = teamDetails.getString("teamCaptain")
                        val teamTagLine = teamDetails.getString("teamTagLine")
                        val teamAchievements = teamDetails.getString("teamAchievements")
                        val teamLogo = teamDetails.getString("teamLogo")
                        val teamMembers = teamDetails.getJSONArray("teamMembers")

                        // Prepare the data for the team
                        val teamData = JSONObject().apply {
                            put("teamName", teamName)
                            put("gameName", gameName)
                            put("teamDetails", teamDetailsText)
                            put("teamLocation", teamLocation)
                            put("teamEmail", teamEmail)
                            put("teamCaptain", teamCaptain)
                            put("teamTagLine", teamTagLine)
                            put("teamAchievements", teamAchievements)
                            put("teamLogo", teamLogo)
                        }

                        fillTeamData(teamData)
                        val firebaseUids = mutableListOf<String>()
                        for (i in 0 until teamMembers.length()) {
                            firebaseUids.add(teamMembers.getString(i))
                        }
                        fetchTeamMembersDetails(firebaseUids)

                    } else {
                        Toast.makeText(this, "Team details not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    // Handle JSON parsing error
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing team details", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                // Handle error from the request
                error.printStackTrace()
                Toast.makeText(this, "Error fetching team details", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBody(): ByteArray {
                return requestBody.toString().toByteArray(Charset.forName("utf-8"))
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Add the request to the request queue (Assuming you have a RequestQueue instance)
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun fetchTeamMembersDetails(firebaseUids: List<String>) {
        val membersList = mutableListOf<UserData>()

        // Create a counter to keep track of how many members we have fetched
        var membersFetched = 0
        val totalMembers = firebaseUids.size

        // Function to fetch user details using their Firebase UID
        fun fetchUserDetails(firebaseUid: String) {
            FirebaseManager.getCurrentUserId()?.let { userId ->
                Log.d("fetchTeam", "Fetching user details for $firebaseUid")

                // Fetch user details from Firebase using the same route as fetchTeamCaptainDetails
                database.child(firebaseUid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(UserData::class.java)
                        if (user != null) {
                            membersList.add(user)
                            membersFetched++

                            Log.d("fetchTeam", "Fetched $membersFetched members")

                            // When we've fetched all the members, update the adapter
                            if (membersFetched == totalMembers) {
                                Log.d("fetchTeam", "All members fetched. Updating adapter.")
                                managePlayersAdapter.updateTeamMembers(membersList)
                            }
                        } else {
                            Log.d("fetchTeam", "User data not found for UID: $firebaseUid")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error (log or show a Toast)
                        Log.e("fetchTeam", "Error fetching user details: ${error.message}")
                    }
                })
            }
        }

        // Start fetching data for all team members
        if (firebaseUids.isEmpty()) {
            Log.d("fetchTeam", "No team members to fetch.")
            return // Return early if there are no members
        }

        for (firebaseUid in firebaseUids) {
            fetchUserDetails(firebaseUid)
        }
    }

    private fun fillTeamData(response: JSONObject) {
        teamNameTextView.text = response.getString("teamName")
        gameNameTextView.text = response.getString("gameName")
        locationTextView.text = response.getString("teamLocation")
        taglineTextView.text = response.getString("teamTagLine")
        teamEmailTextView.text = response.getString("teamEmail")
        teamAchievementsTextView.text = response.getString("teamAchievements")
        teamDetailsTextView.text = response.getString("teamDetails")

        Glide.with(this)
            .load(response.getString("teamLogo"))
            .placeholder(R.drawable.battlegrounds_icon_background)
            .error(R.drawable.battlegrounds_icon_background)
            .into(teamLogoImageView)
        val teamCaptainUid = response.getString("teamCaptain")
        fetchTeamCaptainDetails(teamCaptainUid)
    }

    private fun fetchTeamCaptainDetails(firebaseUid: String) {
        FirebaseManager.getCurrentUserId()?.let { userId ->
            // Fetch captain details from Firebase using the firebaseUid
            database.child(firebaseUid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserData::class.java)
                    if (user != null) {
                        // Populate team captain's details in the UI
                        teamCaptainTextView.text = user.fullname
                        Glide.with(this@viewOwnTeam)
                            .load(user.profilePicture ?: R.drawable.battlegrounds_icon_background)
                            .placeholder(R.drawable.battlegrounds_icon_background)
                            .error(R.drawable.battlegrounds_icon_background)
                            .circleCrop()
                            .into(teamCaptainProfilePicture)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors if needed
                }
            })
        }
    }

    private fun searchUsers(query: String) {
        val usersList = mutableListOf<UserData>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserData::class.java)
                    if (user != null && user.userId != FirebaseManager.getCurrentUserId()) {
                        if (user.fullname.contains(query, ignoreCase = true) ||
                            user.gamerTag.contains(query, ignoreCase = true)
                        ) {
                            if (!usersList.any { it.userId == user.userId }) {
                                usersList.add(user)
                            }
                        }
                    }
                }
                searchUserAdapter.updatePlayersList(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ManageAdmins", "Search error: ${error.message}")
            }
        })
    }

    private fun addPlayer(userId: String) {
        val context: Context = this
        val currentUser = FirebaseManager.getCurrentUserId()
        Log.d("AddPlayer", "Adding player: $userId, current user: $currentUser")
        Log.d("AddPlayer", "Organization: $organizationName, Team: $teamName")
        val url = "${Constants.SERVER_URL}manageTeams/addPlayer"

        val requestBody = JSONObject().apply {
            put("organizationName", organizationName)
            put("teamName", teamName)
            put("currentUserId", currentUser)
            put("playerId", userId)
        }

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                Log.d("AddPlayer", "Player added successfully")
                Toast.makeText(context, "Player added successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, viewOwnTeam::class.java)
                intent.putExtra("organizationName", organizationName)
                intent.putExtra("teamName", teamName)
                startActivity(intent)
            },
            Response.ErrorListener { error ->
                error.networkResponse?.let { response ->
                    val responseData = String(response.data, Charsets.UTF_8)
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val errorMessage = jsonResponse.optString("error", "An unknown error occurred")

                        when (errorMessage) {
                            "Player is already in the team" ->
                                Toast.makeText(context, "This player is already in the team", Toast.LENGTH_LONG).show()
                            "Player is already in another team for this game" ->
                                Toast.makeText(context, "This player is already in another team for the same game", Toast.LENGTH_LONG).show()
                            "Current user not found" ->
                                Toast.makeText(context, "Your account is not recognized. Please log in again.", Toast.LENGTH_LONG).show()
                            "Player user not found" ->
                                Toast.makeText(context, "The selected player does not exist.", Toast.LENGTH_LONG).show()
                            "Team not found" ->
                                Toast.makeText(context, "Team not found. Please check the name.", Toast.LENGTH_LONG).show()
                            else ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }

                        Log.e("AddPlayer", "Error: $errorMessage")
                    } catch (e: Exception) {
                        Log.e("AddPlayer", "Error parsing response: ${e.message}")
                        Toast.makeText(context, "Failed to add player. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    Log.e("AddPlayer", "Unknown error: ${error.message}")
                    Toast.makeText(context, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        ) {
            override fun getBody() = requestBody.toString().toByteArray(Charsets.UTF_8)
            override fun getHeaders() = mutableMapOf("Content-Type" to "application/json")
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun removePlayer(userId: String) {
        val context: Context = this
        val currentUser = FirebaseManager.getCurrentUserId()
        Log.d("RemovePlayer", "Removing player: $userId, Current User: $currentUser")

        val url = "${Constants.SERVER_URL}manageTeams/removePlayer"

        val requestBody = JSONObject().apply {
            put("organizationName", organizationName)
            put("teamName", teamName)
            put("currentUserId", currentUser)
            put("playerId", userId)
        }

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.optString("message", "Success")

                    if (message == "Player removed successfully") {
                        Log.d("RemovePlayer", "Success: $message")
                        Toast.makeText(context, "Player removed successfully", Toast.LENGTH_SHORT).show()

                        // Refresh UI
                        val intent = Intent(context, viewOwnTeam::class.java).apply {
                            putExtra("organizationName", organizationName)
                            putExtra("teamName", teamName)
                        }
                        startActivity(intent)
                    } else {
                        Log.e("RemovePlayer", "Unexpected response: $response")
                        Toast.makeText(context, "Unexpected response. Try again.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("RemovePlayer", "Error parsing success response: ${e.message}")
                    Toast.makeText(context, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                error.networkResponse?.let { response ->
                    val responseData = String(response.data, Charsets.UTF_8)
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val errorMessage = jsonResponse.optString("error", "An unknown error occurred")

                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e("RemovePlayer", "Error: $errorMessage")

                    } catch (e: Exception) {
                        Log.e("RemovePlayer", "Error parsing error response: ${e.message}")
                        Toast.makeText(context, "Failed to remove player. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    Log.e("RemovePlayer", "Unknown error: ${error.message}")
                    Toast.makeText(context, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        ) {
            override fun getBody() = requestBody.toString().toByteArray(Charsets.UTF_8)
            override fun getHeaders() = mutableMapOf("Content-Type" to "application/json")
        }

        Volley.newRequestQueue(context).add(request)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}