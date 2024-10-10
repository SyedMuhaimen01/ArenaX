package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.RankingData
import com.muhaimen.arenax.overallLeaderboardAdapter.overallLeaderboardAdapter
import org.json.JSONArray
import org.json.JSONObject

class overallLeaderboard : AppCompatActivity() {
    private lateinit var overallLeaderboardRecyclerView: RecyclerView
    private lateinit var overallLeaderboardAdapter: overallLeaderboardAdapter
    private lateinit var backButton: ImageButton

    // Define the base URL for the API
    private val baseUrl = "http://192.168.100.6:3000/leaderboard/rankings" // Replace with your actual API URL

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_overall_leaderboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        overallLeaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView)
        overallLeaderboardRecyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch the rankings from the API
        fetchRankings()
    }

    private fun fetchRankings() {
        // Create a request queue
        val requestQueue = Volley.newRequestQueue(this)

        // Create a JSON array request
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            baseUrl,
            null,
            { response ->
                // Parse the JSON response
                val rankingsList = parseRankings(response)
                // Update the adapter with the new data
                overallLeaderboardAdapter = overallLeaderboardAdapter(rankingsList)
                overallLeaderboardRecyclerView.adapter = overallLeaderboardAdapter
            },
            { error ->
                // Handle the error
                Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest)
    }

    private fun parseRankings(response: JSONArray): List<RankingData> {
        val rankingsList = mutableListOf<RankingData>()

        for (i in 0 until response.length()) {
            val jsonObject: JSONObject = response.getJSONObject(i)
            val name = jsonObject.getString("name")
            val totalHrs = jsonObject.getInt("totalHours")
            val profilePictureUrl = jsonObject.getString("profilePicture") // Fetch the URL
            val rank = jsonObject.getInt("rank")
            val gamerTag = jsonObject.getString("gamertag")

            // Create a RankingData instance and add it to the list
            val rankingData = RankingData(
                name = name,
                totalHrs = totalHrs,
                profilePicture = profilePictureUrl,
                rank = rank,
                gamerTag = gamerTag
            )
            rankingsList.add(rankingData)
        }

        return rankingsList
    }
}
