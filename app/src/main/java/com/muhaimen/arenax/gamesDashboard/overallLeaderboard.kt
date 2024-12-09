package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.RankingData
import com.muhaimen.arenax.overallLeaderboardAdapter.overallLeaderboardAdapter
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class overallLeaderboard : AppCompatActivity() {
    private lateinit var overallLeaderboardRecyclerView: RecyclerView
    private lateinit var overallLeaderboardAdapter: overallLeaderboardAdapter
    private lateinit var backButton: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var profilePicture:ImageView
    private lateinit var name:TextView
    private lateinit var totalHours:TextView
    private lateinit var rank:TextView
    private lateinit var gamerTag:TextView
    private lateinit var currentUserGamerTag: String
    private var rankingsList = mutableListOf<RankingData>()
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

        profilePicture = findViewById(R.id.profilePicture)
        name = findViewById(R.id.nameTextView)
        totalHours = findViewById(R.id.totalHours)
        rank = findViewById(R.id.rankNumber)
        gamerTag = findViewById(R.id.gamerTagTextView)


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
        val userId = FirebaseManager.getCurrentUserId()
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userDataRef: DatabaseReference = database.reference.child("userData").child(userId)
            userDataRef.get().addOnSuccessListener { dataSnapshot ->
                currentUserGamerTag = dataSnapshot.child("gamerTag").getValue(String::class.java).toString()

                // Call getCurrentUserRanking only after gamerTag is fetched
                val currentUserRanking = getCurrentUserRanking(rankingsList, currentUserGamerTag)
                if (currentUserRanking != null) {
                    name.text = currentUserRanking.name
                    totalHours.text = "${currentUserRanking.totalHrs}"

                    val uri = currentUserRanking.profilePicture

                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.circle)
                        .error(R.drawable.circle)
                        .circleCrop()
                        .into(profilePicture)

                    // Handle the rank text, checking for "Unranked"
                    if (currentUserRanking.rank == "Unranked") {
                        rank.text = "∞"
                        rank.setTextColor(resources.getColor(R.color.white))
                    } else {
                        rank.text = currentUserRanking.rank.toString()
                        rank.setTextColor(resources.getColor(R.color.white))
                    }

                    gamerTag.text = currentUserRanking.gamerTag
                }
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Error fetching user data", exception)
            }
        } else {
            Log.e("Firebase", "User is not logged in.")
        }


    }

    private fun fetchRankings() {
        val requestQueue = Volley.newRequestQueue(this)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            baseUrl, // Ensure this URL matches your backend endpoint
            null,
            { response ->
                val rankingsList = parseRankings(response)
                overallLeaderboardAdapter = overallLeaderboardAdapter(rankingsList)
                overallLeaderboardRecyclerView.adapter = overallLeaderboardAdapter
                swipeRefreshLayout.isRefreshing = false
            },
            { error ->
                Log.e("Error", "Error fetching rankings: ${error.message}")
                swipeRefreshLayout.isRefreshing = false
                loadRankingsFromPreferences()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }


    private fun parseRankings(response: JSONArray): List<RankingData> {


        for (i in 0 until response.length()) {
            val jsonObject: JSONObject = response.getJSONObject(i)
            val name = jsonObject.getString("name")
            val totalHrs = jsonObject.getInt("totalHours")
            val profilePictureUrl = jsonObject.getString("profilePicture")
            val rank = jsonObject.getInt("rank")
            val gamerTag = jsonObject.getString("gamertag")

            // Add a check to handle unranked users explicitly if necessary
            val rankingData = RankingData(
                name = name,
                totalHrs = totalHrs,
                profilePicture = profilePictureUrl,
                rank = if (rank == 0) "Unranked" else rank.toString(), // Set 'Unranked' for rank 0
                gamerTag = gamerTag
            )
            rankingsList.add(rankingData)
        }

        saveRankingsToPreferences(rankingsList)
        Log.e("Rankings", rankingsList.toString())
        return rankingsList
    }


    private fun saveRankingsToPreferences(rankings: List<RankingData>) {
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
                val rank = jsonObject.getString("rank") // Rank is stored as a string, can be "Unranked" or an integer
                val totalHrs = jsonObject.getInt("totalHours")

                rankingsList.add(RankingData(name, gamerTag, profilePicture, rank, totalHrs))
            }

            rankingsList
        } else {
            emptyList()
        }
    }

    private fun getCurrentUserRanking(rankingsList: List<RankingData>, currentUserGamerTag: String): RankingData? {
        return rankingsList.find { it.gamerTag == currentUserGamerTag }
    }


    override fun onResume() {
        super.onResume()
        val rankings = loadRankingsFromPreferences()
        overallLeaderboardAdapter = overallLeaderboardAdapter(rankings)
        overallLeaderboardRecyclerView.adapter = overallLeaderboardAdapter
    }
}