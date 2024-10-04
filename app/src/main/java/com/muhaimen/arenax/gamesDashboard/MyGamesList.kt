package com.muhaimen.arenax.gamesDashboard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.series.DataPoint
import com.muhaimen.arenax.R
import com.muhaimen.arenax.userProfile.AnalyticsAdapter
import com.muhaimen.arenax.userProfile.AnalyticsData

class MyGamesList : AppCompatActivity() {
    private lateinit var myGamesListRecyclerView: RecyclerView
    private lateinit var myGamesListAdapter: myGamesListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_games_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        myGamesListRecyclerView = findViewById(R.id.myGamesListRecyclerView)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Load sample data into the analytics adapter
        val sampleData = loadSampleAnalyticsData()
        myGamesListAdapter = myGamesListAdapter(sampleData)
        myGamesListRecyclerView.adapter = myGamesListAdapter
    }
    // Sample function to load analytics data
    private fun loadSampleAnalyticsData(): List<AnalyticsData> {
        // Example data points for graph (Hours vs Days)
        val hoursData1 = listOf(
            DataPoint(1.0, 2.0),
            DataPoint(2.0, 3.0),
            DataPoint(3.0, 5.0),
            DataPoint(4.0, 7.0),
            DataPoint(5.0, 4.0)
        )
        val hoursData2 = listOf(
            DataPoint(1.0, 1.0),
            DataPoint(2.0, 2.5),
            DataPoint(3.0, 4.5),
            DataPoint(4.0, 6.0),
            DataPoint(5.0, 5.0)
        )

        // Create AnalyticsData instances
        val game1 = AnalyticsData(
            gameName = "Game 1",
            totalHours = 15,
            iconResId = R.drawable.game_icon_foreground,
            hoursData = hoursData1
        )

        val game2 = AnalyticsData(
            gameName = "Game 2",
            totalHours = 20,
            iconResId = R.drawable.game_icon_foreground,
            hoursData = hoursData2
        )

        // Return a list of analytics data
        return listOf(game1, game2)
    }
}