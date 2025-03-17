package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Event
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class closedEvents : Fragment() {
    private lateinit var closedEventsAdapter: closedEventsAdapter
    private lateinit var closedEventsRecyclerView: RecyclerView
    private lateinit var searchEventsRecyclerView: RecyclerView
    private lateinit var searchEventsAdapter: searchEventsAdapter
    private lateinit var searchBar: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchButton: ImageButton
    private var organizationName: String? = null
    private var eventList: MutableList<Event> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        organizationName = arguments?.getString("organization_name") // Retrieve organization name
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upcomming_events, container, false)

        closedEventsRecyclerView = view.findViewById(R.id.upcomingEventsRecyclerview)
        searchEventsRecyclerView = view.findViewById(R.id.searchEventsRecyclerView)
        searchBar = view.findViewById(R.id.searchbar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        searchButton = view.findViewById(R.id.searchButton)

        // Setup RecyclerViews
        closedEventsRecyclerView.layoutManager = LinearLayoutManager(context)
        closedEventsAdapter = closedEventsAdapter(eventList)
        closedEventsRecyclerView.adapter = closedEventsAdapter

        searchEventsAdapter = searchEventsAdapter(emptyList())
        searchEventsRecyclerView.adapter = searchEventsAdapter
        searchEventsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Show search results only when typing
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    // Show closed events if search bar is empty
                    searchEventsRecyclerView.visibility = View.GONE
                    closedEventsRecyclerView.visibility = View.VISIBLE
                    fetchClosedEvents()
                } else {
                    // Show search results while typing
                    searchEventsRecyclerView.visibility = View.VISIBLE
                    closedEventsRecyclerView.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Fetch events when the fragment loads
        fetchClosedEvents()

        searchButton.setOnClickListener {
            val eventName = searchBar.text.toString().trim()

            if (eventName.isEmpty()) {
                // If search bar is empty, reset UI and fetch closed events
                searchEventsRecyclerView.visibility = View.GONE
                closedEventsRecyclerView.visibility = View.VISIBLE
                fetchClosedEvents()
            } else {
                // Fetch search results but return to main event list
                fetchClosedEventsByName(eventName)
                searchEventsRecyclerView.visibility = View.GONE
                closedEventsRecyclerView.visibility = View.VISIBLE
            }
        }

        // Refresh the event list when pulled down
        swipeRefreshLayout.setOnRefreshListener {
            fetchClosedEvents()
        }

        return view
    }


    private fun fetchClosedEvents() {
        if (organizationName.isNullOrEmpty()) {
            Toast.makeText(context, "Organization not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "${Constants.SERVER_URL}manageEvents/fetchClosedOrganizationEvents"

        val requestQueue = Volley.newRequestQueue(requireContext())

        val requestBody = JSONObject().apply {
            put("organization_name", organizationName)
        }

        val jsonRequest = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            Response.Listener { response ->
                val eventsArray = response.optJSONArray("events") // ✅ Extract JSON array from response
                if (eventsArray != null) {
                    eventList = parseEventResponse(eventsArray)
                    closedEventsAdapter.updateData(eventList)
                } else {
                    Toast.makeText(context, "No closed events found!", Toast.LENGTH_SHORT).show()
                }
                swipeRefreshLayout.isRefreshing = false
            },
            Response.ErrorListener { error ->
                Toast.makeText(context, "Failed to load closed events: ${error.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Content-Type" to "application/json")
            }
        }

        requestQueue.add(jsonRequest)
    }

    private fun fetchClosedEventsByName(eventName: String) {
        if (organizationName.isNullOrEmpty()) {
            Toast.makeText(context, "Organization not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "${Constants.SERVER_URL}manageEvents/fetchClosedOrganizationEventsByName"

        val requestQueue = Volley.newRequestQueue(requireContext())

        val requestBody = JSONObject().apply {
            put("organization_name", organizationName)
            put("event_name", eventName)
        }

        val jsonRequest = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            Response.Listener { response ->
                val eventsArray = response.optJSONArray("events")
                Log.d("Events", "Raw JSON Response: $response")

                if (eventsArray != null) {
                    val parsedEvents = parseEventResponse(eventsArray)

                    requireActivity().runOnUiThread {
                        eventList.clear()
                        eventList.addAll(parsedEvents)
                        closedEventsAdapter.notifyDataSetChanged()
                    }

                    Log.d("Events", "Updated Adapter with ${parsedEvents.size} events")
                } else {
                    Toast.makeText(context, "No matching events found!", Toast.LENGTH_SHORT).show()
                }
                swipeRefreshLayout.isRefreshing = false
            },
            Response.ErrorListener { error ->
                Toast.makeText(context, "Failed to load events: ${error.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Content-Type" to "application/json")
            }
        }

        requestQueue.add(jsonRequest)
    }

    private fun parseEventResponse(response: JSONArray): MutableList<Event> {
        val events = mutableListOf<Event>()
        for (i in 0 until response.length()) {
            val obj = response.getJSONObject(i)
            val event = Event(
                eventId = obj.getString("event_id"),
                organizationId = obj.getString("organization_id"),
                eventName = obj.getString("event_name"),
                gameName = obj.getString("game_name"),
                eventMode = obj.getString("event_mode"),
                platform = obj.getString("platform"),
                location = obj.optString("location", ""),
                eventDescription = obj.optString("event_description", ""),
                startDate = obj.optString("start_date", ""),
                endDate = obj.optString("end_date", ""),
                startTime = obj.optString("start_time", ""),
                endTime = obj.optString("end_time", ""),
                eventLink = obj.optString("event_link", ""),
                eventBanner = obj.optString("event_banner", "")
            )
            events.add(event)
        }
        return events
    }
}
