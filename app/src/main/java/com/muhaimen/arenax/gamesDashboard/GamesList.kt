package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

class gamesList : AppCompatActivity() {
    private lateinit var gamesListRecyclerView: RecyclerView
    private lateinit var gamesListAdapter: gamesListAdapter
    private lateinit var gamesSearchBar: AutoCompleteTextView
    private lateinit var originalGamesList: List<gamesData>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_games_list)

        // Enable edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the RecyclerView
        gamesListRecyclerView = findViewById(R.id.gamesListRecyclerView)
        gamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Initialize the search bar
        gamesSearchBar = findViewById(R.id.searchbar)

        // Load sample data into the list
        originalGamesList = loadSampleGamesListData()

        // Initialize the adapter with the full list of games
        gamesListAdapter = gamesListAdapter(originalGamesList)
        gamesListRecyclerView.adapter = gamesListAdapter

        // Set up autocomplete suggestions based on the game names
        setupAutoComplete()

        // Filter games as the user types in the search bar
        setupSearchFilter()
    }

    private fun loadSampleGamesListData(): List<gamesData> {
        val game1 = gamesData(
            gameName = "Game 1",
            genre = listOf("Shooting"),
            iconResId = R.drawable.game_icon_foreground,
            publisher = "Publisher 1"
        )
        val game2 = gamesData(
            gameName = "Game 2",
            genre = listOf("Racing"),
            iconResId = R.drawable.game_icon_foreground,
            publisher = "Publisher 2"
        )

        // Add more games as needed
        return listOf(game1, game2)
    }

    private fun setupAutoComplete() {
        // Extract game names for autocomplete suggestions
        val gameNames = originalGamesList.map { it.gameName }

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
            originalGamesList
        } else {
            originalGamesList.filter {
                it.gameName.contains(query, ignoreCase = true)
            }
        }

        // Update the adapter with the filtered list
        gamesListAdapter.updateGamesList(filteredList)
    }
}
