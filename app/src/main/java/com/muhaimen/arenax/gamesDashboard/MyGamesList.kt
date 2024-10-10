package com.muhaimen.arenax.gamesDashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.AnalyticsData
import android.widget.AutoCompleteTextView
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.utils.Constants
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MyGamesList : AppCompatActivity() {
    private lateinit var myGamesListRecyclerView: RecyclerView
    private lateinit var myGamesListAdapter: MyGamesListAdapter
    private lateinit var gamesSearchBar: AutoCompleteTextView
    private lateinit var myGamesList: List<AnalyticsData>
    private lateinit var addGame: ImageButton
    private lateinit var refreshButton: Button
    lateinit var backButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private val client = OkHttpClient()
    private val sharedPreferences by lazy { getSharedPreferences("MyGamesPrefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_games_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        gamesSearchBar = findViewById(R.id.searchbar)
        addGame = findViewById(R.id.addGame)
        backButton = findViewById(R.id.backButton)
        refreshButton = findViewById(R.id.refreshButton)
        myGamesListRecyclerView = findViewById(R.id.myGamesListRecyclerView)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this)

        myGamesListAdapter = MyGamesListAdapter(emptyList())
        myGamesListRecyclerView.adapter = myGamesListAdapter


        loadGamesFromPreferences()

        backButton.setOnClickListener {
            finish()
        }
        addGame.setOnClickListener {
            val intent = Intent(this, gamesList::class.java)
            startActivity(intent)
        }

        // Set up the refresh button's click listener
        refreshButton.setOnClickListener {
            showRefreshDialog()
        }

        addGame.setOnClickListener {
            val intent = Intent(this, gamesList::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadGamesFromPreferences()
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
            .url("${Constants.SERVER_URL}usergames/user/${auth.currentUser?.uid}/mygames")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MyGamesList, "Failed to fetch games", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@MyGamesList, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateEmptyGameList() {
        with(sharedPreferences.edit()) {
            putString("gamesList", "[]")
            apply()
        }
        runOnUiThread {
            myGamesListAdapter.updateGamesList(emptyList())
            Toast.makeText(this@MyGamesList, "No games found", Toast.LENGTH_SHORT).show() // Optional feedback
        }
    }

    private fun saveGamesToPreferences(gamesJson: String) {
        with(sharedPreferences.edit()) {
            putString("gamesList", gamesJson)
            apply()
        }
    }

    private fun parseGamesData(responseBody: String) {
        try {
            val jsonObject = JSONObject(responseBody)
            val gamesArray = jsonObject.getJSONArray("games")

            myGamesList = List(gamesArray.length()) { index ->
                val gameObject = gamesArray.getJSONObject(index)
                Log.d("MyGamesList", "Parsing game: ${gameObject.getString("gameName")}, Icon URL: ${gameObject.getString("gameIcon")}")
                AnalyticsData(
                    gameName = gameObject.getString("gameName"),
                    totalHours = gameObject.getInt("totalHours"),
                    iconResId = gameObject.getString("gameIcon"),
                    graphData = emptyList()
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

    private fun showRefreshDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Do you want to refresh the game list?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> fetchUserGames() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        val alert = dialogBuilder.create()
        alert.setTitle("Refresh Games")
        alert.show()
    }
}
