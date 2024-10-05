package com.muhaimen.arenax.gamesDashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.series.DataPoint
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.AnalyticsData
import android.widget.AutoCompleteTextView


class MyGamesList : AppCompatActivity() {
    private lateinit var myGamesListRecyclerView: RecyclerView
    private lateinit var myGamesListAdapter: myGamesListAdapter
    private lateinit var gamesSearchBar: AutoCompleteTextView
    private lateinit var myGamesList: List<AnalyticsData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_games_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        gamesSearchBar = findViewById(R.id.searchbar)

        myGamesListRecyclerView = findViewById(R.id.myGamesListRecyclerView)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Load sample data into the analytics adapter
        myGamesList= loadSampleAnalyticsData()
        val sampleData = loadSampleAnalyticsData()
        myGamesListAdapter = myGamesListAdapter(sampleData)
        myGamesListRecyclerView.adapter = myGamesListAdapter

        setupAutoComplete()
        setupSearchFilter()
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
    private fun setupAutoComplete() {
        // Extract game names for autocomplete suggestions
        val gameNames = myGamesList.map { it.gameName }

        // Create an ArrayAdapter for AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, gameNames)
        gamesSearchBar.setAdapter(adapter)

        // Set up the item click listener for auto-complete suggestions
        gamesSearchBar.setOnItemClickListener { _, _, position, _ ->
            // When a suggestion is selected, filter the list based on the selected game
            val selectedGameName = gamesSearchBar.adapter.getItem(position).toString()
            filterGamesList(selectedGameName)
        }
    }

    private fun setupSearchFilter() {
        gamesSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the RecyclerView as user types
                filterGamesList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterGamesList(query: String) {
        // Filter the list of games based on the search query
        val filteredList = if (query.isEmpty()) {
            myGamesList
        } else {
            myGamesList.filter {
                it.gameName.contains(query, ignoreCase = true)
            }
        }

        // Update the adapter with the filtered list
        myGamesListAdapter.updateGamesList(filteredList)
    }
}