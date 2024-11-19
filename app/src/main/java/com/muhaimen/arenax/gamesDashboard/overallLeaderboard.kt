package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.RankingData
import com.muhaimen.arenax.overallLeaderboardAdapter.overallLeaderboardAdapter
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class overallLeaderboard : AppCompatActivity() {
    private lateinit var overallLeaderboardRecyclerView: RecyclerView
    private lateinit var overallLeaderboardAdapter: overallLeaderboardAdapter
    private lateinit var backButton: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val sharedPreferences by lazy { getSharedPreferences("Leaderboard", Context.MODE_PRIVATE) }

    private val baseUrl = "${Constants.SERVER_URL}leaderboard/rankings"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_overall_leaderboard)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)
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

        fetchRankings()
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primaryColor)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            fetchRankings()

        }
    }

    private fun fetchRankings() {
        val requestQueue = Volley.newRequestQueue(this)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            baseUrl,
            null,
            { response ->
                val rankingsList = parseRankings(response)
                overallLeaderboardAdapter = overallLeaderboardAdapter(rankingsList)
                overallLeaderboardRecyclerView.adapter = overallLeaderboardAdapter
                swipeRefreshLayout.isRefreshing = false
            },
            { error ->
             //   Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    private fun parseRankings(response: JSONArray): List<RankingData> {
        val rankingsList = mutableListOf<RankingData>()

        for (i in 0 until response.length()) {
            val jsonObject: JSONObject = response.getJSONObject(i)
            val name = jsonObject.getString("name")
            val totalHrs = jsonObject.getInt("totalHours")
            val profilePictureUrl = jsonObject.getString("profilePicture")
            val rank = jsonObject.getInt("rank")
            val gamerTag = jsonObject.getString("gamertag")

            val rankingData = RankingData(
                name = name,
                totalHrs = totalHrs,
                profilePicture = profilePictureUrl,
                rank = rank,
                gamerTag = gamerTag
            )
            rankingsList.add(rankingData)

        }
        saveRankingsToPreferences(rankingsList)
        Log.e("Rankings", rankingsList.toString())
        return rankingsList
    }

    private fun saveRankingsToPreferences(rankings: List<RankingData>) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val jsonArray = JSONArray()
        for (ranking in rankings) {
            val jsonObject = JSONObject().apply {
                put("name", ranking.name)
                put("gamerTag", ranking.gamerTag)
                put("profilePicture", ranking.profilePicture)
                put("rank", ranking.rank)
                put("totalHours", ranking.totalHrs)
            }
            jsonArray.put(jsonObject)
        }

        editor.putString("rankingsList", jsonArray.toString())
        editor.apply()
    }

    private fun loadRankingsFromPreferences(): List<RankingData> {

        val jsonString = sharedPreferences.getString("rankingsList", null)

        return if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)
            val rankingsList = mutableListOf<RankingData>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val name = jsonObject.getString("name")
                val gamerTag = jsonObject.getString("gamerTag")
                val profilePicture = jsonObject.getString("profilePicture")
                val rank = jsonObject.getInt("rank")
                val totalHrs = jsonObject.getInt("totalHours")

                rankingsList.add(RankingData(name, gamerTag, profilePicture, rank, totalHrs))
            }

            rankingsList
        } else {
            emptyList()
        }
    }

    override fun onResume() {
        super.onResume()
        val rankings = loadRankingsFromPreferences()
        overallLeaderboardAdapter = overallLeaderboardAdapter(rankings)
        overallLeaderboardRecyclerView.adapter = overallLeaderboardAdapter
    }
}