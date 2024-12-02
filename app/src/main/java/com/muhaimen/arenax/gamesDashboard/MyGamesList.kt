package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.AnalyticsData
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.dataClasses.GameAnalytics
import com.muhaimen.arenax.explore.ExplorePage
import com.muhaimen.arenax.gamesDashboard.ViewGameAnalytics.DateValueFormatter
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.userFeed.UserFeed
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.utils.Constants
import okhttp3.*
import org.json.JSONObject
import java.io.IOException



class MyGamesList : AppCompatActivity() {
    private lateinit var myGamesListRecyclerView: RecyclerView
    private lateinit var myGamesListAdapter: MyGamesListAdapter
    private lateinit var gamesSearchBar: AutoCompleteTextView
    private lateinit var myGamesList: List<AnalyticsData>
    private lateinit var postButton:ImageView
    private lateinit var profileButton:ImageView
    private lateinit var addGame: ImageView
    private lateinit var exploreButton: ImageView
    private lateinit var homeButton: LinearLayout
    lateinit var backButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var playtimeBarChart: BarChart
    private val client = OkHttpClient()
    private var isGameAdded = false
    private lateinit var userId:String
    private val sharedPreferences by lazy { getSharedPreferences("MyGamesPrefs", Context.MODE_PRIVATE) }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_games_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        gamesSearchBar = findViewById(R.id.searchbar)
        addGame = findViewById(R.id.addGame)
        backButton = findViewById(R.id.backButton)

        myGamesListRecyclerView = findViewById(R.id.myGamesListRecyclerView)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this)

        postButton = findViewById(R.id.addPostButton)
        postButton.setOnClickListener {
            val intent = Intent(this, UploadContent::class.java)
            startActivity(intent)
        }

        homeButton = findViewById(R.id.home)
        homeButton.setOnClickListener {
            val intent = Intent(this, UserFeed::class.java)
            startActivity(intent)
        }
        exploreButton = findViewById(R.id.exploreButton)
        exploreButton.setOnClickListener {
            val intent = Intent(this, ExplorePage::class.java)
            startActivity(intent)
        }

        profileButton = findViewById(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }


        myGamesListAdapter = MyGamesListAdapter(emptyList(), userId)
        myGamesListRecyclerView.adapter = myGamesListAdapter

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primaryColor)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            fetchUserGames()
            swipeRefreshLayout.isRefreshing = false
        }

        loadGamesFromPreferences()

        backButton.setOnClickListener {
            finish()
        }
        addGame.setOnClickListener {
            val intent = Intent(this, gamesList::class.java)
            startActivity(intent)
        }


        addGame.setOnClickListener {
            val intent = Intent(this, gamesList::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("MyGamesList", "onResume called ")
        isGameAdded = false
        val filter = IntentFilter("NEW_GAME_ADDED")
        LocalBroadcastManager.getInstance(this).registerReceiver(gameBroadcastReceiver, filter)
        if (!isGameAdded) {
            loadGamesFromPreferences()
        }
    }


    private val gameBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("BroadcastReceiver", "Game added broadcast received")
            isGameAdded = true
            fetchUserGames()
        }
    }


    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gameBroadcastReceiver)
    }

    private fun loadGamesFromPreferences() {
        val jsonString = sharedPreferences.getString("gamesList", null)
        if (jsonString != null) {
            parseGamesData(jsonString)
        } else {
            fetchUserGames()
        }
    }

    private fun fetchUserGames() {
        val request = Request.Builder()
            .url("${Constants.SERVER_URL}usergames/user/${userId}/mygames")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    // Uncomment the next line to show a toast message on failure
                    // Toast.makeText(this@MyGamesList, "Failed to fetch games", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    if (responseBody.isNotEmpty()) {
                        parseGamesData(responseBody)
                        saveGamesToPreferences(responseBody)
                    } else {
                        updateEmptyGameList()
                    }
                } else {
                    runOnUiThread {
                        // Toast.makeText(this@MyGamesList, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun parseGamesData(responseBody: String) {
        try {
            val jsonObject = JSONObject(responseBody)
            val gamesArray = jsonObject.getJSONArray("games")

            myGamesList = List(gamesArray.length()) { index ->
                val gameObject = gamesArray.getJSONObject(index)
                Log.d("MyGamesList", "Parsing game: ${gameObject.getString("gameName")}, Icon URL: ${gameObject.getString("gameIcon")}")

                // Extract the graph data as a List of Pair<Date, TotalHours>
                val graphDataArray = gameObject.getJSONArray("graphData")
                val graphData = List(graphDataArray.length()) { gIndex ->
                    val dataPoint = graphDataArray.getJSONObject(gIndex)
                    val date = dataPoint.getString("date") // Get date
                    val totalHours = dataPoint.getDouble("totalHours") // Get total hours
                    Pair(date, totalHours)
                }

                // Create AnalyticsData object
                AnalyticsData(
                    gameName = gameObject.getString("gameName"),
                    totalHours = gameObject.getDouble("totalHours"), // Assuming totalHours is a double
                    iconResId = gameObject.getString("gameIcon"),
                    graphData = graphData // Assign the graph data
                )
            }

            runOnUiThread {
                Log.d("MyGamesList", "Number of games fetched: ${myGamesList.size}")
                myGamesListAdapter.updateGamesList(myGamesList)
                setupAutoComplete()
                setupSearchFilter()
            }
        } catch (e: Exception) {
            Log.e("MyGamesList", "Error parsing games data", e)
        }
    }


    private fun updateEmptyGameList() {
        with(sharedPreferences.edit()) {
            putString("gamesList", "[]")
            apply()
        }
        runOnUiThread {
            myGamesListAdapter.updateGamesList(emptyList())
         //   Toast.makeText(this@MyGamesList, "No games found", Toast.LENGTH_SHORT).show() // Optional feedback
        }
    }

    private fun saveGamesToPreferences(gamesJson: String) {
        with(sharedPreferences.edit()) {
            putString("gamesList", gamesJson)
            apply()
        }
    }



    private fun setupAutoComplete() {
        val gameNames = myGamesList.map { it.gameName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, gameNames)
        gamesSearchBar.setAdapter(adapter)

        gamesSearchBar.setOnItemClickListener { _, _, position, _ ->
            val selectedGameName = gamesSearchBar.adapter.getItem(position).toString()
            filterGamesList(selectedGameName)
        }
    }

    private fun setupSearchFilter() {
        gamesSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterGamesList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterGamesList(query: String) {
        val filteredList = if (query.isEmpty()) {
            myGamesList
        } else {
            myGamesList.filter {
                it.gameName.contains(query, ignoreCase = true)
            }
        }
        myGamesListAdapter.updateGamesList(filteredList)
    }
    private fun fetchUserGameStats(game: String) {
        val url = "${Constants.SERVER_URL}analytics/gameAnalytics"
        val userId = intent.getStringExtra("userId")

        val requestBody = JSONObject().apply {
            put("gameName", game)
            put("userId", userId)
        }

        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            com.android.volley.Request.Method.POST, url, requestBody,
            { response ->
                Log.d(TAG, "Response received: $response")
            },
            { error ->
                Log.e(TAG, "Error fetching data: ${error.message}")
                error.printStackTrace()
            })

        queue.add(jsonObjectRequest)
    }
    // Session Frequency Bar Chart
    private fun playtimeBarChart(sessionFrequencyPerDay: List<Int>, dates: List<String>) {
        val entries = sessionFrequencyPerDay.mapIndexed { index, frequency -> BarEntry(index.toFloat(), frequency.toFloat()) }

        val barDataSet = BarDataSet(entries, "Total Playtime Hrs Distribution").apply {
            color = Color.parseColor("#339966")  // Muted green
            valueTextSize = 12f
        }

        playtimeBarChart.apply {
            data = BarData(barDataSet)
            xAxis.apply {
                valueFormatter = DateValueFormatter(dates)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f
                textSize = 12f
                setDrawGridLines(false)
            }
            axisLeft.isEnabled = false
            axisRight.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) = "${value.toInt()}h"
                }
                textSize = 12f
                granularity = 1f
                setDrawGridLines(false)
            }
            description.isEnabled = false
            legend.isEnabled = false
            setExtraOffsets(15f, 15f, 15f, 15f)
            invalidate()
        }
    }


    // Function to fetch game analytics for the user
    private fun fetchUserGameStats() {
        val userId = auth.currentUser?.uid ?: return // Ensure user is authenticated
        val url = "${Constants.SERVER_URL}gameAnalytics/user/$userId/gameStats" // Modify URL to include userId in the path

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null, // No body for GET requests
            { response ->
                Log.d("GameAnalytics", "Successfully fetched game stats: $response")
                try {
                    parseAndPopulateCharts(response) // Parse and populate charts with received data
                } catch (e: Exception) {
                    Log.e("GameAnalytics", "Error parsing response: ${e.message}")
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("GameAnalytics", "Error fetching game stats: ${error.message}")
                error.printStackTrace()
                handleError() // Handle error (e.g., notify user or retry)
            }
        )

        queue.add(request) // Add the request to the queue
    }


    // Function to display the fetched game analytics (just a placeholder for your UI logic)
    fun displayGameAnalytics(gameAnalyticsList: List<GameAnalytics>) {
        // You can update your UI here, for example, display it in a RecyclerView
        gameAnalyticsList.forEach {
            println("Game: ${it.gameName}, Package: ${it.packageName}, Total Playtime: ${it.totalPlaytime}")
        }
    }
}
