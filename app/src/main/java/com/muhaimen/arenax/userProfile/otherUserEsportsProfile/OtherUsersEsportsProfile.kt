package com.muhaimen.arenax.userProfile.otherUserEsportsProfile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myOrganizations.MyOrganizationsAdapter
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myTeams.MyTeamsAdapter
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class OtherUsersEsportsProfile : AppCompatActivity() {
    private lateinit var teamsRecyclerView: RecyclerView
    private lateinit var teamsAdapter: otherUserTeamsAdapter
    private lateinit var organizationsRecyclerView: RecyclerView
    private lateinit var organizationsAdapter: otherUserOrganizationsAdapter
    private var teamsList: MutableList<Team> = mutableListOf()
    private val organizationList = mutableListOf<OrganizationData>()
    private lateinit var userId:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_other_users_esports_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        userId = intent.getStringExtra("userId").toString()

        // button listeners initialization

        teamsRecyclerView = findViewById(R.id.teamsRecyclerView)
        teamsRecyclerView.layoutManager = LinearLayoutManager(this)
        teamsAdapter = otherUserTeamsAdapter(teamsList)
        teamsRecyclerView.adapter = teamsAdapter

        organizationsRecyclerView = findViewById(R.id.organizationsRecyclerView)
        organizationsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        organizationsAdapter = otherUserOrganizationsAdapter(organizationList)
        organizationsRecyclerView.adapter = organizationsAdapter



        fetchTeams(userId)
        fetchOrganizations()

    }

    private fun fetchTeams(firebaseUid: String) {
        val url = "${Constants.SERVER_URL}manageTeams/myTeams/$firebaseUid"
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val teamsJsonArray: JSONArray = response.getJSONArray("teams")
                    val teamsList = mutableListOf<Team>()

                    // Parsing the JSON array
                    for (i in 0 until teamsJsonArray.length()) {
                        val teamJsonObject: JSONObject = teamsJsonArray.getJSONObject(i)
                        val team = Team(
                            teamName = teamJsonObject.getString("team_name"),
                            gameName = teamJsonObject.getString("game_name"),
                            teamDetails = "",  // You can modify this based on the API response
                            teamLocation = "",  // Same here
                            teamEmail = "",  // Same here
                            teamCaptain = "",  // Same here
                            teamTagLine = "",  // Same here
                            teamAchievements = "",  // Same here
                            teamLogo = teamJsonObject.getString("team_logo"),
                            teamMembers = null // Adjust based on response if needed
                        )
                        teamsList.add(team)
                    }

                    // Populating the adapter with the data
                    teamsAdapter.updateData(teamsList)

                } catch (e: Exception) {
                    Log.e("FetchTeams", "Error parsing response", e)
                }
            },
            { error ->
                Log.e("FetchTeams", "Error: ${error.message}")
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchOrganizations() {
        val url = "${Constants.SERVER_URL}registerOrganization/user/organizations"
        val requestQueue = Volley.newRequestQueue(this)

        // Create JSON body
        val requestBody = JSONObject().apply {
            put("firebaseUid", userId) // Send firebaseUid in the body
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response: JSONObject ->
                organizationList.clear() // Clear previous data

                val organizationsArray = response.optJSONArray("organizations") ?: JSONArray()
                for (i in 0 until organizationsArray.length()) {
                    val orgObject = organizationsArray.getJSONObject(i)
                    val organization = OrganizationData(
                        organizationId = orgObject.getString("organization_id"),
                        organizationName = orgObject.getString("organization_name"),
                        organizationLogo = orgObject.optString("organization_logo", null),
                        organizationLocation = orgObject.optString("organization_location", null)
                    )
                    organizationList.add(organization)
                }

                organizationsAdapter.notifyDataSetChanged() // Refresh RecyclerView
            },
            { error ->
                Toast.makeText(this, "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}