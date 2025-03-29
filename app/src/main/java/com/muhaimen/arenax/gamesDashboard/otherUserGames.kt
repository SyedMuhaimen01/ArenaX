package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.explore.ExplorePage
import com.muhaimen.arenax.gamesDashboard.ViewGameAnalytics.DateValueFormatter
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.userFeed.UserFeed
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.utils.Constants
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import java.util.*


import java.io.IOException

import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject

class otherUserGames : AppCompatActivity() {
    private lateinit var myGamesListRecyclerView: RecyclerView
    private lateinit var myGamesListAdapter: MyGamesListAdapter
    private lateinit var gamesSearchBar: AutoCompleteTextView
    private lateinit var myGamesList: List<AnalyticsData>
    private lateinit var postButton:FrameLayout
    private lateinit var talentExchangeButton:LinearLayout
    private lateinit var profileButton:LinearLayout
    private lateinit var exploreButton: LinearLayout
    private lateinit var homeButton: LinearLayout
    lateinit var backButton: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var playtimeBarChart: BarChart
    private val client = OkHttpClient()
    private lateinit var receivedUserId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_other_user_games)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        receivedUserId = intent.getStringExtra("userId").toString()
        gamesSearchBar = findViewById(R.id.searchbar)
        backButton = findViewById(R.id.backButton)

        playtimeBarChart = findViewById(R.id.totalPlaytimeBarChart)
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

        talentExchangeButton=findViewById(R.id.esportsButton)
        talentExchangeButton.setOnClickListener {
            val intent = Intent(this, switchToEsports::class.java)
            intent.putExtra("loadedFromActivity","casual")
            startActivity(intent)
        }

        myGamesListAdapter = MyGamesListAdapter(emptyList(), receivedUserId)
        myGamesListRecyclerView.adapter = myGamesListAdapter

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primaryColor)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            fetchUserGames()
            fetchUserGameStats()
            swipeRefreshLayout.isRefreshing = false
        }

        fetchUserGameStats()
        fetchUserGames()

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserGames() {
        val request = Request.Builder()
            .url("${Constants.SERVER_URL}usergames/user/${receivedUserId}/mygames")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {}
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    if (responseBody.isNotEmpty()) {
                        parseGamesData(responseBody)
                    } else {
                        myGamesListAdapter.updateGamesList(emptyList())
                    }
                } else {
                    runOnUiThread {
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
                    val date = dataPoint.getString("date")
                    val totalHours = dataPoint.getDouble("totalHours")
                    Pair(date, totalHours)
                }

                // Create AnalyticsData object
                AnalyticsData(
                    gameName = gameObject.getString("gameName"),
                    totalHours = gameObject.getDouble("totalHours"),
                    iconResId = gameObject.getString("gameIcon"),
                    graphData = graphData
                )
            }
            runOnUiThread {
                myGamesListAdapter.updateGamesList(myGamesList)
                setupAutoComplete()
                setupSearchFilter()
            }
        } catch (e: Exception) {
            Log.e("MyGamesList", "Error parsing games data", e)
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

    //Function not used in the current implemented app logic
    private fun fetchUserGameStats(game: String) {
        val url = "${Constants.SERVER_URL}analytics/gameAnalytics"

        val requestBody = JSONObject().apply {
            put("gameName", game)
            put("userId", receivedUserId)
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

    private fun playtimeBarChart(sessionFrequencyPerDay: MutableList<Float>, gameNames: List<String>) {
        // Create BarEntries for each game with its playtime
        val entries = sessionFrequencyPerDay.mapIndexed { index, frequency -> BarEntry(index.toFloat(), frequency.toFloat()) }

        val barDataSet = BarDataSet(entries, "Total Playtime Hrs Distribution").apply {
            color = Color.parseColor("#339966")  // Muted green
            valueTextSize = 12f
        }

        playtimeBarChart.apply {
            // Set the bar data for the chart
            data = BarData(barDataSet)
            xAxis.apply {
                // Set game names on the X-axis
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in gameNames.indices) gameNames[index] else ""
                    }
                }
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f
                textSize = 12f // Set text size for X-axis labels
                setDrawGridLines(false) // Disable grid lines for X-axis
            }
            axisLeft.isEnabled = false // Disable left Y-axis
            axisRight.apply {
                // Set the Y-axis formatter
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}h" // Format Y-axis to show hours
                    }
                }
                textSize = 12f // Set text size for Y-axis labels
                granularity = 1f // Set the Y-axis granularity
                setDrawGridLines(false) // Disable grid lines for Y-axis
            }
            description.isEnabled = false // Disable chart description
            legend.isEnabled = false // Disable chart legend
            setExtraOffsets(15f, 15f, 15f, 15f) // Add extra padding around the chart
            invalidate()  // Redraw the chart
        }
    }

    private fun fetchUserGameStats() {
        val url = "${Constants.SERVER_URL}analytics/user/${receivedUserId}/hoursPerGame"
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GameAnalytics", "Error fetching game stats: ${e.message}")
                e.printStackTrace()
                runOnUiThread {  }

            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("GameAnalytics", "Unsuccessful response: ${response.code}")
                    runOnUiThread { }
                    return
                }

                response.body?.let { responseBody ->
                    try {
                        val jsonResponse = JSONObject(responseBody.string())
                        Log.d("GameAnalytics", "Successfully fetched game stats: $jsonResponse")
                        runOnUiThread { parseAndPopulateCharts(jsonResponse) }
                    } catch (e: Exception) {
                        Log.e("GameAnalytics", "Error parsing response: ${e.message}")
                        e.printStackTrace()
                        runOnUiThread {  }
                    }
                }
            }
        })
    }

    private fun parseAndPopulateCharts(response: JSONObject) {
        try {
            val gameStats = response.getJSONArray("gameAnalytics")

            val playtimeList = mutableListOf<Float>()
            val gameNamesList = mutableListOf<String>()

            for (i in 0 until gameStats.length()) {
                val game = gameStats.getJSONObject(i)
                val playtime = game.getString("totalPlaytime").toFloatOrNull() ?: 0f
                val gameName = game.getString("gameName")

                playtimeList.add(playtime)
                gameNamesList.add(gameName)
            }
            playtimeBarChart(playtimeList, gameNamesList)

        } catch (e: JSONException) {
            Log.e("GameAnalytics", "Error parsing game stats JSON: ${e.message}")
            e.printStackTrace()
        }
    }
}
