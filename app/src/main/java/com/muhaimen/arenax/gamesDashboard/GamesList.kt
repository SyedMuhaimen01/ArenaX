package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.api.RetrofitClient
import com.muhaimen.arenax.dataClasses.ApiResponse
import com.muhaimen.arenax.dataClasses.gamesData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class gamesList : AppCompatActivity() {
    private lateinit var gamesListRecyclerView: RecyclerView
    private lateinit var gamesListAdapter: gamesListAdapter
    private lateinit var gamesSearchBar: AutoCompleteTextView
    private var originalGamesList: List<gamesData> = emptyList() // Initialize as empty list

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_games_list)

        Log.d("GamesListActivity", "onCreate: Activity started")  // Log activity creation

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

        // Log before data is loaded
        Log.d("GamesListActivity", "onCreate: Loading games data")

        // Load data into the list
        loadGamesListData()  // Call to fetch games data

        // Initialize the adapter with an empty list (to be updated later)
        gamesListAdapter = gamesListAdapter(originalGamesList)
        gamesListRecyclerView.adapter = gamesListAdapter
    }

    private fun loadGamesListData() {
        Log.d("GamesListActivity", "loadGamesListData: Fetching games from API")

        val call = RetrofitClient.instance.getGames() // Ensure getGames() is defined correctly in your Retrofit service

        call.enqueue(object : Callback<List<ApiResponse>> {
            override fun onResponse(call: Call<List<ApiResponse>>, response: Response<List<ApiResponse>>) {
                Log.d("GamesListActivity", "onResponse: Received response from API")

                if (response.isSuccessful) {
                    Log.d("GamesListActivity", "onResponse: Successful response")

                    response.body()?.let { apiResponseList ->
                        Log.d("GamesListActivity", "onResponse: Games list size - ${apiResponseList.size}")

                        // Convert ApiResponse to GamesData using the fromApiResponse method
                        originalGamesList = apiResponseList.map { apiResponse ->
                            gamesData.fromApiResponse(apiResponse) // Use the companion object method
                        }

                        // Update the adapter with the fetched list of games
                        gamesListAdapter.updateGamesList(originalGamesList)

                        // Set up autocomplete suggestions after data is loaded
                        setupAutoComplete()
                        setupSearchFilter()
                    }
                } else {
                    // Handle the case where the response was not successful
                    Log.e("GamesListActivity", "onResponse: Failed to fetch games - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<ApiResponse>>, t: Throwable) {
                // Handle network failure
                Log.e("GamesListActivity", "onFailure: Network request failed - ${t.message}")
            }
        })
    }

    private fun setupAutoComplete() {
        Log.d("GamesListActivity", "setupAutoComplete: Setting up autocomplete suggestions")

        // Extract game names for autocomplete suggestions
        val gameNames = originalGamesList.map { it.gameName }
        Log.d("GamesListActivity", "setupAutoComplete: Game names - $gameNames")

        // Create an ArrayAdapter for AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, gameNames)
        gamesSearchBar.setAdapter(adapter)

        // Set up the item click listener for auto-complete suggestions
        gamesSearchBar.setOnItemClickListener { _, _, position, _ ->
            // When a suggestion is selected, filter the list based on the selected game
            val selectedGameName = gamesSearchBar.adapter.getItem(position).toString()
            Log.d("GamesListActivity", "onItemClick: Selected game name - $selectedGameName")

            // Filter the list based on selected game
            filterGamesList(selectedGameName)
        }
    }

    private fun setupSearchFilter() {
        gamesSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the list based on the text entered in the search bar
                val searchQuery = s.toString()
                filterGamesList(searchQuery)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterGamesList(query: String) {
        Log.d("GamesListActivity", "filterGamesList: Filtering games with query - $query")

        // Filter games based on the query
        val filteredList = if (query.isEmpty()) {
            originalGamesList // Show all games if query is empty
        } else {
            originalGamesList.filter { it.gameName.contains(query, ignoreCase = true) } // Filter by game name
        }

        // Update the adapter with the filtered list
        gamesListAdapter.updateGamesList(filteredList)
        Log.d("GamesListActivity", "filterGamesList: Filtered list size - ${filteredList.size}")
    }
}
