package com.muhaimen.arenax.esportsManagement.battlegrounds

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Event
import com.muhaimen.arenax.esportsManagement.esportsProfile.esportsProfile
import com.muhaimen.arenax.esportsManagement.exploreEsports.exploreEsports
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONException

class battlegrounds : AppCompatActivity() {
    private lateinit var talentExchangeButton: ImageView
    private lateinit var battlegroundsButton: ImageView
    private lateinit var switchButton: ImageView
    private lateinit var exploreButton: ImageView
    private lateinit var profileButton: ImageView
    private lateinit var searchbar: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var battlegroundsAdapter: BattlegroundsAdapter
    private lateinit var searchEventsAdapter: SearchEventsAdapter
    private lateinit var requestQueue: RequestQueue
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId:String
    private var eventList: MutableList<Event> = mutableListOf()
    private var searchEventList: List<Event> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_battlegrounds)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        database=FirebaseDatabase.getInstance()
        auth=FirebaseAuth.getInstance()
        userId=auth.currentUser?.uid.toString()
        // Initialize Views
        talentExchangeButton = findViewById(R.id.talentExchangeButton)
        battlegroundsButton = findViewById(R.id.battlegroundsButton)
        switchButton = findViewById(R.id.switchButton)
        exploreButton = findViewById(R.id.exploreButton)
        profileButton = findViewById(R.id.profileButton)
        eventsRecyclerView = findViewById(R.id.events_recyclerview)
        searchRecyclerView = findViewById(R.id.searchEventsRecyclerView)
        searchbar = findViewById(R.id.searchbar)

        // Set RecyclerView Adapters
        battlegroundsAdapter = BattlegroundsAdapter(eventList)
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventsRecyclerView.adapter = battlegroundsAdapter

        searchEventsAdapter = SearchEventsAdapter(searchEventList)
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchEventsAdapter

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this)

        // Fetch events from backend
        fetchEvents()

        // Handle search bar focus
        searchbar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                eventsRecyclerView.visibility = View.GONE
                searchRecyclerView.visibility = View.VISIBLE
            }
        }

        // Handle search text input
        searchbar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.trim() ?: ""
                filterSearchResults(searchText)
            }
        })

        // Set Button Click Listeners
        talentExchangeButton.setOnClickListener {
            startActivity(Intent(this, talentExchange::class.java))
        }

        battlegroundsButton.setOnClickListener {
            startActivity(Intent(this, battlegrounds::class.java))
        }

        switchButton.setOnClickListener {
            startActivity(Intent(this, switchToEsports::class.java))
        }

        exploreButton.setOnClickListener {
            startActivity(Intent(this, exploreEsports::class.java))
        }

        profileButton.setOnClickListener {
            startActivity(Intent(this, esportsProfile::class.java))
        }
    }

    private fun fetchEvents() {
        val url = "${Constants.SERVER_URL}manageBattleGrounds/fetchEvents/${userId}"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    Log.d("EventsAPI", "Raw JSON Response: $response")

                    // Parse JSON response to match Event class
                    eventList = parseEvents(response)

                    Log.d("Events", "Parsed Events: $eventList")

                    // Update adapter with new data
                    battlegroundsAdapter.updateEvents(eventList)
                } catch (e: Exception) {
                    Log.e("Events", "Parsing error: ${e.message}")
                    Toast.makeText(this, "Error parsing events data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("Events", "API Error: ${error.message}")
                Toast.makeText(this, "Error fetching events: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(jsonArrayRequest)
    }

    private fun parseEvents(response: JSONArray): MutableList<Event> {
        val eventList = mutableListOf<Event>()
        try {
            for (i in 0 until response.length()) {
                val jsonObject = response.getJSONObject(i)

                val event = Event(
                    eventId = jsonObject.optString("event_id", ""),
                    organizationId = jsonObject.optString("organization_id", ""),
                    eventName = jsonObject.optString("event_name", ""),
                    gameName = jsonObject.optString("game_name", ""),
                    eventMode = jsonObject.optString("event_mode", ""),
                    platform = jsonObject.optString("platform", ""),
                    location = jsonObject.optString("location", null),
                    eventDescription = jsonObject.optString("event_description", null),
                    startDate = jsonObject.optString("start_date", null),
                    endDate = jsonObject.optString("end_date", null),
                    startTime = jsonObject.optString("start_time", null),
                    endTime = jsonObject.optString("end_time", null),
                    eventLink = jsonObject.optString("event_link", null),
                    eventBanner = jsonObject.optString("event_banner", null)
                )

                eventList.add(event)
            }
        } catch (e: JSONException) {
            Log.e("parseEvents", "JSON Parsing error: ${e.message}")
        }
        return eventList
    }


    private fun filterSearchResults(query: String) {
        searchEventList = eventList.filter { it.eventName.contains(query, ignoreCase = true) }

        //searchEventsAdapter.updateEvents(searchEventList)
    }
}
