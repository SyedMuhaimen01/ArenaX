package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myTeams

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Event
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class myTeamsFragment : Fragment() {

    companion object {
        fun newInstance() = myTeamsFragment()
    }

    private val viewModel: MyTeamsViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyTeamsAdapter // Replace with your adapter
    private var teamsList: MutableList<Team> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_my_teams, container, false)
        recyclerView = root.findViewById(R.id.myTeamsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MyTeamsAdapter(teamsList)
        recyclerView.adapter = adapter

        val firebaseUid = FirebaseManager.getCurrentUserId().toString()
        fetchTeams(firebaseUid)

        return root
    }

    // Function to fetch teams using Volley
    private fun fetchTeams(firebaseUid: String) {
        val url = "${Constants.SERVER_URL}manageTeams/myTeams/$firebaseUid"
        val requestQueue = Volley.newRequestQueue(requireContext())

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
                    adapter.updateData(teamsList)

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
}
