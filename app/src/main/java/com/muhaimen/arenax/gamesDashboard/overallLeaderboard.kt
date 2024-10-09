package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.series.DataPoint
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.AnalyticsData
import com.muhaimen.arenax.dataClasses.RankingData
import com.muhaimen.arenax.overallLeaderboardAdapter.overallLeaderboardAdapter
import com.muhaimen.arenax.userProfile.AnalyticsAdapter

class overallLeaderboard : AppCompatActivity() {
    private lateinit var overallLeaderboardRecyclerView: RecyclerView
    private lateinit var overallLeaderboardAdapter: overallLeaderboardAdapter
    private lateinit var backButton: ImageButton
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
        overallLeaderboardRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Load sample data into the analytics adapter
        val sampleData = loadSampleLeaderboardData()
       overallLeaderboardAdapter= overallLeaderboardAdapter(sampleData)
       overallLeaderboardRecyclerView.adapter = overallLeaderboardAdapter
    }
    // Sample function to load analytics data
    private fun loadSampleLeaderboardData(): List<RankingData> {
        // Example data points for graph (Hours vs Days)


        // Create AnalyticsData instances
        val rank1 = RankingData(
            name = "Muhaimen",
            totalHrs = 888,
            profilePicture = R.drawable.game_icon_foreground,
            rank = 1,
            gamerTag = "mYm XEROXXX"
        )
        val rank2 = RankingData(
            name = "Mustafa",
            totalHrs = 886,
            profilePicture = R.drawable.game_icon_foreground,
            rank = 2,
            gamerTag = "mYm KINGPIN"
        )
        val rank3 = RankingData(
            name = "Farhan",
            totalHrs = 123,
            profilePicture = R.drawable.game_icon_foreground,
            rank = 3,
            gamerTag = "mYm J7"
        )
        val rank4 = RankingData(
            name = "Player4",
            totalHrs = 123,
            profilePicture = R.drawable.game_icon_foreground,
            rank = 4,
            gamerTag = "user987"
        )
        val rank5 = RankingData(
            name = "Player5",
            totalHrs = 123,
            profilePicture = R.drawable.game_icon_foreground,
            rank = 5,
            gamerTag = "619"
        )



        // Return a list of analytics data
        return listOf(rank1, rank2, rank3, rank4, rank5)
    }
}