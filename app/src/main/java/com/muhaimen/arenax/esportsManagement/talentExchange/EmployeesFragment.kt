package com.muhaimen.arenax.esportsManagement.talentExchange

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.dataClasses.JobWithUserDetails
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class EmployeesFragment : Fragment() {

    private lateinit var employeesAdapter: EmployeesAdapter
    private lateinit var recyclerView: RecyclerView
    private var jobList: MutableList<JobWithUserDetails> = mutableListOf()
    private lateinit var firebaseUid: String
    private lateinit var searchBar: AutoCompleteTextView
    private lateinit var searchButton: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recruitForSpinner: Spinner
    private val organizationList: MutableList<OrganizationData> = mutableListOf()
    private var selectedOrganization: OrganizationData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_employees, container, false)

        // Initialize RecyclerViews
        recyclerView = view.findViewById(R.id.employees_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Initialize Spinner
        recruitForSpinner = view.findViewById(R.id.recruitForSpinner)

        // Fetch organizations and populate the Spinner
        fetchOrganizations(recruitForSpinner)
        // Initialize the adapter with an empty list
        employeesAdapter = EmployeesAdapter(jobList,selectedOrganization)
        recyclerView.adapter = employeesAdapter

        // Initialize search bar and button
        searchBar = view.findViewById(R.id.searchbar)
        searchButton = view.findViewById(R.id.searchButton)

        // Fetch initial data
        firebaseUid = FirebaseManager.getCurrentUserId().toString()
        fetchJobAvailabilityData()

        // Set up search button click listener
        searchButton.setOnClickListener {
            val searchText = searchBar.text.toString().trim()
            if (searchText.isNotEmpty()) {
                searchJobsData(searchText) // Perform search if text is not empty
            } else {
                fetchJobAvailabilityData()
            }
        }

        // Clear search when the search bar is cleared
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && searchBar.text.toString().trim().isEmpty()) {
                fetchJobAvailabilityData() // Repopulate with original data

                recyclerView.visibility = View.VISIBLE
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    fetchJobAvailabilityData()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        swipeRefreshLayout.setOnRefreshListener {
            fetchJobAvailabilityData()
            swipeRefreshLayout.isRefreshing = false
        }

        return view
    }

    private fun fetchJobAvailabilityData() {
        val url = "${Constants.SERVER_URL}manageTalentXchange/fetchJobAvailability/$firebaseUid"

        // Create a Volley request to fetch job availability data
        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,  // No body needed for GET request
            { response ->
                // Handle the response
                Log.d("Volley", "Response: $response")
                parseJobAvailabilityResponse(response)            },
            { error ->
                // Handle error
                Log.e("Volley", "Error fetching job availability data: ${error.message}")
                Toast.makeText(context, "Error fetching job availability data", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the Volley request queue
        Volley.newRequestQueue(context).add(request)
    }

    private fun searchJobsData(searchText: String) {
        val url = "${Constants.SERVER_URL}manageTalentXchange/searchUserJobAvailability"

        // Create a JSON object with the search text
        val jsonBody = JSONObject().apply {
            put("searchText", searchText)
            put("firebaseUid", firebaseUid)
        }

        // Use JsonObjectRequest to send a POST request with the JSON body
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody, // Pass the JSON body here
            { response ->
                try {
                    // Extract the jobs array from the response
                    val jobsArray = response.getJSONArray("jobs")
                    parseJobAvailabilityResponse(jobsArray)


                    recyclerView.visibility = View.GONE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error parsing search results", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("Volley", "Error searching jobs: ${error.message}")
                Toast.makeText(context, "Error searching jobs", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the Volley request queue
        Volley.newRequestQueue(context).add(request)
    }

    private fun parseJobAvailabilityResponse(response: JSONArray) {
        try {
            // Clear the existing list of jobs
            jobList.clear()

            // Loop through the JSON array and parse each job
            for (i in 0 until response.length()) {
                val jobObject = response.getJSONObject(i)

                // Parse Job data
                val jobId = jobObject.optString("availability_id", "Unknown") // Backend uses "availability_id"
                val userId = jobObject.optString("user_id", "Unknown")
                val jobTitle = jobObject.optString("job_title", "Unknown")
                val jobType = jobObject.optString("job_type", "Unknown")
                val jobLocation = jobObject.optString("job_location", "Unknown")
                val jobDescription = jobObject.optString("job_description", "No description available")
                val workplaceType = jobObject.optString("workplace_type", "Unknown")
                val tags = jobObject.optJSONArray("tags")?.let { tagArray ->
                    (0 until tagArray.length()).map { tagArray.getString(it) }
                } ?: emptyList()

                // Create a Job object
                val job = Job(
                    jobId = jobId,
                    organizationId = "", // Not used in this context
                    jobTitle = jobTitle,
                    jobType = jobType,
                    jobLocation = jobLocation,
                    jobDescription = jobDescription,
                    workplaceType = workplaceType,
                    tags = tags
                )

                // Parse User Details
                val userDetailsObject = jobObject.optJSONObject("user_details")
                val fullName = userDetailsObject?.optString("full_name", "Unknown User") // Backend uses "full_name"
                val gamerTag = userDetailsObject?.optString("gamer_tag", "No Gamer Tag") // Backend uses "gamer_tag"
                val profilePictureUrl = userDetailsObject?.optString("profile_picture_url", null)

                // Create a UserData object
                val userData = UserData(
                    userId = userId,
                    fullname = fullName?: "Unknown User",
                    email = "", // Not provided in the response
                    dOB = "", // Not provided in the response
                    gamerTag = gamerTag?: "No Gamer Tag",
                    profilePicture = profilePictureUrl,
                    gender = Gender.PreferNotToSay, // Default value
                    bio = null, // Not provided in the response
                    location = null, // Not provided in the response
                    accountVerified = false, // Default value
                    playerId = null, // Not provided in the response
                    rank = null // Not provided in the response
                )

                // Combine Job and UserData into a JobWithUserDetails object
                val jobWithUserDetails = JobWithUserDetails(job, userData)

                // Add job to the list
                jobList.add(jobWithUserDetails)
            }

            // Notify the adapter of the data change
            employeesAdapter.updateJobList(jobList)
            Log.d("Volley", "Job list updated")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error parsing job data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchOrganizations(recruitForSpinner: Spinner) {
        val url = "${Constants.SERVER_URL}registerOrganization/user/organizations"
        val requestQueue = Volley.newRequestQueue(requireContext())

        // Create JSON body
        val requestBody = JSONObject().apply {
            put("firebaseUid", FirebaseManager.getCurrentUserId())
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response: JSONObject ->
                organizationList.clear() // Clear previous data

                val organizationsArray = response.optJSONArray("organizations") ?: JSONArray()
                for (i in 0 until organizationsArray.length()) {
                    val orgObject = organizationsArray.getJSONObject(i)
                    val organization = OrganizationData(
                        organizationId = orgObject.getString("organization_id"),
                        organizationName = orgObject.getString("organization_name"),
                        organizationLogo = orgObject.optString("organization_logo", null),
                        organizationLocation = orgObject.optString("organization_location", null)
                    )
                    organizationList.add(organization)
                }

                // Set the custom adapter to the Spinner
                val adapter = OrganizationSpinnerAdapter(requireContext(), organizationList)
                recruitForSpinner.adapter = adapter

                // Set the onItemSelectedListener
                recruitForSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedOrganization = organizationList[position]
                        Toast.makeText(
                            requireContext(),
                            "Selected Organization: ${selectedOrganization?.organizationName}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Update the adapter with the selected organization
                        employeesAdapter.updateOrganization(selectedOrganization)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Do nothing
                    }
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}