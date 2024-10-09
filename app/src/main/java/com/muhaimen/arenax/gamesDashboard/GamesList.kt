package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.R
import com.muhaimen.arenax.api.RetrofitClient
import com.muhaimen.arenax.dataClasses.AppInfo
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class gamesList : AppCompatActivity() {
    private lateinit var gamesListRecyclerView: RecyclerView
    private lateinit var gamesListAdapter: gamesListAdapter
    private lateinit var gamesSearchBar: AutoCompleteTextView
    private lateinit var originalGamesList: MutableList<AppInfo>
    private lateinit var auth: FirebaseAuth
    private lateinit var refreshButton: Button
    private val sharedPrefsName = "games_prefs"
    private val gamesKey = "stored_games"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_games_list)
        auth = FirebaseAuth.getInstance()
        originalGamesList = mutableListOf()
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

        // Initialize the refresh button
        refreshButton = findViewById(R.id.refreshButton) // Make sure to have this button in your XML layout
        refreshButton.setOnClickListener {
            showRefreshDialog()
        }

        // Log before data is loaded
        Log.d("GamesListActivity", "onCreate: Loading games data")

        gamesListAdapter = gamesListAdapter(originalGamesList, auth.currentUser?.uid.toString())
        gamesListRecyclerView.adapter = gamesListAdapter

        loadGamesFromSharedPreferences()
    }

    private fun showRefreshDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Refresh Game List")
            .setMessage("Do you want to refresh the game list to check for newly installed games?")
            .setPositiveButton("Yes") { _, _ -> fetchInstalledApps() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun loadGamesFromSharedPreferences() {
        val sharedPrefs = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val storedGamesJson = sharedPrefs.getString(gamesKey, null)

        if (storedGamesJson != null) {
            // If games exist in SharedPreferences, load them
            originalGamesList = mutableListOf()
            val gamesArray = JSONArray(storedGamesJson)
            for (i in 0 until gamesArray.length()) {
                val game = gamesArray.getJSONObject(i)
                val appInfo = AppInfo(
                    name = game.getString("name"),
                    packageName = game.getString("packageName"),
                    genre = game.getString("genre"),
                    publisher = game.getString("publisher"),
                    logoUrl = game.getString("logo")
                )
                originalGamesList.add(appInfo)
            }

            // Update the adapter with the stored list of games
            gamesListAdapter.updateGamesList(originalGamesList)
            setupAutoComplete()
            setupSearchFilter()
        } else {
            // No games stored, fetch from the server
            fetchInstalledApps()
        }
    }

    private fun fetchInstalledApps() {
        val pm: PackageManager = packageManager
        val apps = pm.getInstalledApplications(0)

        val appArray = JSONArray()
        for (app in apps) {
            if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val appObject = JSONObject().apply {
                    put("name", app.loadLabel(pm).toString())
                    put("packageName", app.packageName)
                }
                appArray.put(appObject)
            }
        }

        sendAppsToBackend(appArray)
    }

    private fun sendAppsToBackend(appArray: JSONArray) {
        val queue = Volley.newRequestQueue(this)
        val url = "http://192.168.100.6:3000/scrapper/user/${auth.currentUser?.uid}/checkApps"

        val jsonBody = JSONObject().apply {
            put("apps", appArray)
        }

        val jsonObjectRequest = JsonObjectRequest(
            com.android.volley.Request.Method.POST, url, jsonBody,
            { response ->
                val gameApps = response.getJSONArray("gameApps")

                // Create a list to store the app info
                val receivedGamesList = mutableListOf<AppInfo>()

                for (i in 0 until gameApps.length()) {
                    val game = gameApps.getJSONObject(i)
                    val appInfo = AppInfo(
                        name = game.getString("name"),
                        packageName = game.getString("packageName"),
                        genre = game.getString("genre"),
                        publisher = game.getString("publisher"),
                        logoUrl = game.getString("logo")
                    )

                    receivedGamesList.add(appInfo)
                }

                originalGamesList = receivedGamesList

                // Update the adapter with the new list of games
                gamesListAdapter.updateGamesList(originalGamesList)

                // Store the fetched games in SharedPreferences
                storeGamesInSharedPreferences(originalGamesList)

                // Set up autocomplete suggestions and filters after data is loaded
                setupAutoComplete()
                setupSearchFilter()
            },
            { error ->
                // Log the entire error response
                error.printStackTrace()
                if (error.networkResponse != null) {
                    val errorResponse = String(error.networkResponse.data)
                    Toast.makeText(this, "Error fetching games: $errorResponse", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error fetching games: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Set a longer timeout
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // Add the request to the queue
        queue.add(jsonObjectRequest)
    }

    private fun storeGamesInSharedPreferences(gamesList: List<AppInfo>) {
        val sharedPrefs = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val gamesArray = JSONArray()

        for (game in gamesList) {
            val gameObject = JSONObject().apply {
                put("name", game.name)
                put("packageName", game.packageName)
                put("genre", game.genre)
                put("publisher", game.publisher)
                put("logo", game.logoUrl)
            }
            gamesArray.put(gameObject)
        }

        editor.putString(gamesKey, gamesArray.toString())
        editor.apply()
    }

    private fun setupAutoComplete() {
        Log.d("GamesListActivity", "setupAutoComplete: Setting up autocomplete suggestions")

        // Extract game names for autocomplete suggestions
        val gameNames = originalGamesList.map { it.name }
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
            val filteredList = originalGamesList.filter { it.name == selectedGameName }
            gamesListAdapter.updateGamesList(filteredList)
        }
    }

    private fun setupSearchFilter() {
        Log.d("GamesListActivity", "setupSearchFilter: Setting up search filter")

        // Add a TextWatcher to the search bar
        gamesSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterGamesList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterGamesList(query: String) {
        Log.d("GamesListActivity", "filterGamesList: Filtering games with query: $query")

        // Filter the original games list based on the query
        val filteredList = originalGamesList.filter { it.name.contains(query, ignoreCase = true) }
        gamesListAdapter.updateGamesList(filteredList)

        // Show a message if no games are found
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No games found", Toast.LENGTH_SHORT).show()
        }
    }
}
