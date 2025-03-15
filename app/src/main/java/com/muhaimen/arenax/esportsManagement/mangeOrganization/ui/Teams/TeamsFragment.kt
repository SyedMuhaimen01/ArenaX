package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class TeamsFragment : Fragment() {
    private lateinit var registerTeamButton: Button
    private lateinit var teamsRecyclerView: RecyclerView
    private lateinit var teamsAdapter: TeamsAdapter
    private val teamsList = mutableListOf<Team>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_teams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve organization name from arguments
        val organizationName = arguments?.getString("organization_name")

        // Initialize RecyclerView
        teamsRecyclerView = view.findViewById(R.id.teamsRecyclerView)
        teamsRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        teamsAdapter = TeamsAdapter(teamsList)
        teamsRecyclerView.adapter = teamsAdapter

        // Fetch teams for the given organization
        fetchTeams(organizationName)

        // Register Team Button functionality
        registerTeamButton = view.findViewById(R.id.registerButton)
        registerTeamButton.setOnClickListener {
            val intent = Intent(activity, registerTeam::class.java).apply {
                putExtra("organization_name", organizationName) // Pass organization name
            }
            startActivity(intent)
        }
    }

    private fun fetchTeams(organizationName: String?) {
        if (organizationName == null) {
            Toast.makeText(requireContext(), "Organization name is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val queue = Volley.newRequestQueue(requireContext())
        val url = "${Constants.SERVER_URL}manageTeams/teams"

        // Create a JSON object with the organization name to send in the request body
        val requestBody = JSONObject()
        try {
            requestBody.put("organizationName", organizationName)
        } catch (e: JSONException) {
            Log.e("TeamsFragment", "Error creating JSON request body", e)
            Toast.makeText(requireContext(), "Failed to create request body", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    val teamsArray = response.optJSONArray("teams") ?: JSONArray()  // Avoid null exception
                    // Clear the list before adding new data
                    teamsList.clear()

                    for (i in 0 until teamsArray.length()) {
                        val teamObject = teamsArray.optJSONObject(i) ?: JSONObject()  // Ensure it's a valid JSON object
                        Log.d("TeamsFragment", "Team object: $teamObject")

                        // Ensure non-null values and extract them properly
                        val teamName = teamObject.optString("team_name", "Unknown Team Name")
                        val gameName = teamObject.optString("game_name", "Unknown Game")
                        val teamDetails = teamObject.optString("team_details", "No Details Available")
                        val teamLocation = teamObject.optString("team_location", "Unknown Location")
                        val teamEmail = teamObject.optString("team_email", "No Email")
                        val teamCaptain = teamObject.optString("team_captain", "No Captain")
                        val teamTagLine = teamObject.optString("team_tagline", "No Tagline")
                        val teamAchievements = teamObject.optString("team_achievements", "No Achievements")
                        val teamLogo = teamObject.optString("team_logo", "")

                        // Ensure teamMembers are handled properly
                        val teamMembers = getTeamMembers(teamObject.optJSONArray("team_members") ?: JSONArray()) // Safely handle missing members

                        // Create the Team object
                        val team = Team(
                            teamName = teamName,
                            gameName = gameName,
                            teamDetails = teamDetails,
                            teamLocation = teamLocation,
                            teamEmail = teamEmail,
                            teamCaptain = teamCaptain,
                            teamTagLine = teamTagLine,
                            teamAchievements = teamAchievements,
                            teamLogo = teamLogo,
                            teamMembers = teamMembers
                        )

                        // Add the team to the list
                        teamsList.add(team)
                        Log.d("TeamsFragment", "Team: ${team.teamName}, ${team.gameName}, ${team.teamLogo}")
                    }

                    // Notify the adapter of changes
                    teamsAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    Log.e("TeamsFragment", "Error parsing teams response", e)
                    //Toast.makeText(requireContext(), "Failed to parse teams data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("TeamsFragment", "Error fetching teams: $error")
                //Toast.makeText(requireContext(), "Failed to fetch teams", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the request queue
        queue.add(jsonObjectRequest)
    }


    private fun getTeamMembers(teamMembersArray: JSONArray): List<String> {
        val membersList = mutableListOf<String>()
        for (i in 0 until teamMembersArray.length()) {
            membersList.add(teamMembersArray.optString(i, ""))
        }
        return membersList
    }
}
