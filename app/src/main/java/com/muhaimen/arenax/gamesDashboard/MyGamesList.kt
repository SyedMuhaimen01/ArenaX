package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.explore.ExplorePage
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
    private lateinit var postButton:ImageButton
    private lateinit var profileButton:ImageButton
    private lateinit var addGame: ImageButton
    private lateinit var exploreButton: ImageButton
    private lateinit var homeButton: ImageButton
    lateinit var backButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
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

        homeButton = findViewById(R.id.homeButton)
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

}
