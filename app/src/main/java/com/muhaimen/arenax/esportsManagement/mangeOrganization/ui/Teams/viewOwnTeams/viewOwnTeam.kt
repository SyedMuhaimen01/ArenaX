package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams

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
    private lateinit var backButton:ImageButton
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

        //fetchTeamDetails()
        // Initialize Firebase Database Reference
        database = FirebaseDatabase.getInstance().reference.child("userData")

        managePlayersAdapter = ManagePlayerAdapter(mutableListOf()) { adminId ->
            // Remove admin from organization logic here
        }

        searchUserAdapter = SearchPlayerAdapter(mutableListOf()) { userId ->
            // Add admin to organization logic here
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

        // Back Button Handling
        backButton.setOnClickListener {
            finish()
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

//    private fun fetchTeamDetails() {
//        val url = "${Constants.SERVER_URL}manageTeams/getTeamDetails"
//        val requestBody = JSONObject().apply {
//            put("teamName", teamName) // Ensure this matches the backend
//        }
//
//        val stringRequest = object : StringRequest(
//            Method.POST, url,
//            Response.Listener { response ->
//                try {
//                    val jsonResponse = JSONObject(response)
//                    if (jsonResponse.has("teamDetails")) {
//                        val teamDetails = jsonResponse.getJSONObject("teamDetails")
//
//                        // Extract data from the response
//                        val teamName = teamDetails.getString("teamName")
//                        val gameName = teamDetails.getString("gameName")
//                        val teamDetailsText = teamDetails.getString("teamDetails")
//                        val teamLocation = teamDetails.getString("teamLocation")
//                        val teamEmail = teamDetails.getString("teamEmail")
//                        val teamCaptain = teamDetails.getString("teamCaptain")
//                        val teamTagLine = teamDetails.getString("teamTagLine")
//                        val teamAchievements = teamDetails.getString("teamAchievements")
//                        val teamLogo = teamDetails.getString("teamLogo")
//                        val teamMembers = teamDetails.getJSONArray("teamMembers")
//
//                        // Handle the retrieved team details as needed
//                        // Fill in the data for the UI using the fillTeamData function
//                        val teamData = JSONObject().apply {
//                            put("teamName", teamName)
//                            put("gameName", gameName)
//                            put("teamDetails", teamDetailsText)
//                            put("teamLocation", teamLocation)
//                            put("teamEmail", teamEmail)
//                            put("teamCaptain", teamCaptain)
//                            put("teamTagLine", teamTagLine)
//                            put("teamAchievements", teamAchievements)
//                            put("teamLogo", teamLogo)
//                        }
//                        fillTeamData(teamData)
//
//                        // Update the adapter with team members (captain + members)
//                        val membersList = mutableListOf<TeamMember>()
//                        val captainMember = TeamMember(
//                            userId = teamCaptain,
//                            fullname = "", // Initially empty, to be filled later
//                            gamerTag = null, // Initially empty, to be filled later
//                            profilePicture = null // Initially empty, to be filled later
//                        )
//                        membersList.add(captainMember)
//
//                        // Get the details of team members
//                        for (i in 0 until teamMembers.length()) {
//                            val member = teamMembers.getJSONObject(i)
//                            val teamMember = TeamMember(
//                                userId = member.getString("userId"),
//                                fullname = member.getString("fullname"),
//                                gamerTag = member.getString("gamerTag"),
//                                profilePicture = member.getString("profilePicture")
//                            )
//                            membersList.add(teamMember)
//                        }
//
//                        val team = Team(
//                            teamName = teamName,
//                            gameName = gameName,
//                            teamDetails = teamDetailsText,
//                            teamLocation = teamLocation,
//                            teamEmail = teamEmail,
//                            teamCaptain = teamCaptain,
//                            teamTagLine = teamTagLine,
//                            teamAchievements = teamAchievements,
//                            teamLogo = teamLogo,
//                            teamMembers = membersList
//                        )
//                        managePlayersAdapter.updateTeamMembers(team)
//
//                    } else {
//                        // Handle error if teamDetails not found in the response
//                        Toast.makeText(this, "Team details not found", Toast.LENGTH_SHORT).show()
//                    }
//                } catch (e: JSONException) {
//                    // Handle JSON parsing error
//                    e.printStackTrace()
//                    Toast.makeText(this, "Error parsing team details", Toast.LENGTH_SHORT).show()
//                }
//            },
//            Response.ErrorListener { error ->
//                // Handle error from the request
//                error.printStackTrace()
//                Toast.makeText(this, "Error fetching team details", Toast.LENGTH_SHORT).show()
//            }
//        ) {
//            override fun getBody(): ByteArray {
//                return requestBody.toString().toByteArray(Charset.forName("utf-8"))
//            }
//
//            override fun getHeaders(): MutableMap<String, String> {
//                val headers = HashMap<String, String>()
//                headers["Content-Type"] = "application/json"
//                return headers
//            }
//        }
//
//        // Add the request to the request queue (Assuming you have a RequestQueue instance)
//        Volley.newRequestQueue(this).add(stringRequest)
//    }
//
//
//
//    private fun fillTeamData(response: JSONObject) {
//        teamNameTextView.text = response.getString("teamName")
//        gameNameTextView.text = response.getString("gameName")
//        locationTextView.text = response.getString("teamLocation")
//        taglineTextView.text = response.getString("teamTagLine")
//        teamCaptainTextView.text = response.getString("teamCaptain")
//        teamEmailTextView.text = response.getString("teamEmail")
//        teamAchievementsTextView.text = response.getString("teamAchievements")
//        teamDetailsTextView.text = response.getString("teamDetails")
//
//        // Load team logo with Glide
//        Glide.with(this)
//            .load(response.getString("teamLogo")) // Firebase URL
//            .placeholder(R.drawable.battlegrounds_icon_background)
//            .error(R.drawable.battlegrounds_icon_background)
//            .into(teamLogoImageView)
//    }
//
//
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

    fun initializeUI()
    {
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
        backButton = findViewById(R.id.backButton)
        addPlayerButton = findViewById(R.id.addPlayerButton)
        searchBar = findViewById(R.id.searchbar)
        searchbarLinearLayout = findViewById(R.id.searchLinearLayout)
        playersRecyclerView.layoutManager = LinearLayoutManager(this)
        searchUserRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}