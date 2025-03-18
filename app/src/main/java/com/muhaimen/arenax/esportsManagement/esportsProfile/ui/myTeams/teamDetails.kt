package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myTeams

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
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
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams.ManagePlayerAdapter
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams.SearchPlayerAdapter
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams.viewOwnTeam
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

class teamDetails : AppCompatActivity() {
    private lateinit var teamLogoImageView: ImageView
    private lateinit var teamNameTextView: TextView
    private lateinit var gameNameTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var taglineTextView: TextView
    private lateinit var teamCaptainTextView: TextView
    private lateinit var teamCaptainProfilePicture: ImageView
    private lateinit var teamDetailsTextView: TextView
    private lateinit var teamEmailTextView: TextView
    private lateinit var teamAchievementsTextView: TextView
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var playersAdapter: playersAdapter
    private lateinit var database: DatabaseReference
    private lateinit var organizationName: String
    private lateinit var teamName: String
    private lateinit var gameName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_team_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        initializeUI()

        teamName = intent.getStringExtra("teamName") ?: ""
        gameName=intent.getStringExtra("gameName")?:""

        fetchTeamDetails()

        database = FirebaseDatabase.getInstance().reference.child("userData")

        playersAdapter = playersAdapter(mutableListOf())
        playersRecyclerView.adapter = playersAdapter


        teamCaptainTextView.text?.toString()?.takeIf { it.isNotEmpty() }?.let { captainId ->
            database.child(captainId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(UserData::class.java)?.let { user ->
                        Glide.with(this@teamDetails)
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
        playersRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
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
                database.child(firebaseUid).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(UserData::class.java)
                        if (user != null) {
                            membersList.add(user)
                            membersFetched++

                            Log.d("fetchTeam", "Fetched $membersFetched members")

                            // When we've fetched all the members, update the adapter
                            if (membersFetched == totalMembers) {
                                Log.d("fetchTeam", "All members fetched. Updating adapter.")
                                playersAdapter.updateTeamMembers(membersList)
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
                        Glide.with(this@teamDetails)
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



    override fun onBackPressed() {
        super.onBackPressed()
    }
}