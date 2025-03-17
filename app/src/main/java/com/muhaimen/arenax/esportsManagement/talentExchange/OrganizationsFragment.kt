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
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.dataClasses.JobWithOrganization
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class OrganizationsFragment : Fragment() {

    private lateinit var organizationsAdapter: OrganizationsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchRecyclerView: RecyclerView
    private var jobWithOrgList: MutableList<JobWithOrganization> = mutableListOf()
    private lateinit var firebaseUid: String
    private lateinit var searchBar: AutoCompleteTextView
    private lateinit var searchButton: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organizations, container, false)

        // Initialize RecyclerViews
        recyclerView = view.findViewById(R.id.organizations_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        searchRecyclerView = view.findViewById(R.id.searchOrganizationsRecyclerView)
        searchRecyclerView.layoutManager = LinearLayoutManager(activity)

        // Initialize the adapter with an empty list
        organizationsAdapter = OrganizationsAdapter(jobWithOrgList)
        searchRecyclerView.adapter = organizationsAdapter
        recyclerView.adapter = organizationsAdapter

        // Initialize search bar and button
        searchBar = view.findViewById(R.id.searchbar)
        searchButton = view.findViewById(R.id.searchButton)

        // Fetch initial data
        firebaseUid = FirebaseManager.getCurrentUserId().toString()
        fetchJobsData(firebaseUid)

        // Set up search button click listener
        searchButton.setOnClickListener {
            val searchText = searchBar.text.toString().trim()
            if (searchText.isNotEmpty()) {
                searchJobs(searchText) // Perform search if text is not empty
            } else {
                fetchJobsData(firebaseUid)
            }
        }

        // Clear search when the search bar is cleared
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && searchBar.text.toString().trim().isEmpty()) {
                fetchJobsData(firebaseUid) // Repopulate with original data
                searchRecyclerView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    fetchJobsData(firebaseUid)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        swipeRefreshLayout.setOnRefreshListener {
            fetchJobsData(firebaseUid)
        }

        return view
    }

    private fun fetchJobsData(firebaseUid: String) {
        val url = "${Constants.SERVER_URL}manageTalentXchange/fetchJobs/$firebaseUid"

        // Create a Volley request to fetch the combined job and organization data
        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,  // No body needed for GET request
            { response ->
                // Handle the response
                Log.d("Volley", "Response: $response")
                clearAndPopulateAdapter(response)
            },
            { error ->
                // Handle error
                Log.e("Volley", "Error fetching jobs data: ${error.message}")
                Toast.makeText(context, "Error fetching jobs data", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the Volley request queue
        Volley.newRequestQueue(context).add(request)
    }

    private fun searchJobs(searchText: String) {
        val url = "${Constants.SERVER_URL}manageTalentXchange/searchJobs"

        // Create a JSON object with the search text
        val jsonBody = JSONObject().apply {
            put("searchText", searchText)
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
                    clearAndPopulateAdapter(jobsArray)

                    // Show search results RecyclerView and hide the original one
                    searchRecyclerView.visibility = View.VISIBLE
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


    private fun clearAndPopulateAdapter(response: JSONArray) {
        // Clear the existing list of jobs
        jobWithOrgList.clear()

        // Parse the response and populate the list
        parseAndPopulateJobs(response)

        // Notify the adapter of the data change
        organizationsAdapter.updateJobWithOrgList(jobWithOrgList)
    }

    private fun parseAndPopulateJobs(response: JSONArray) {
        try {
            // Loop through the JSON array and parse each job
            for (i in 0 until response.length()) {
                val jobObject = response.getJSONObject(i)

                // Parse Job data
                val jobId = jobObject.getString("job_id")
                val organizationId = jobObject.getString("organization_id")
                val jobTitle = jobObject.getString("job_title")
                val jobType = jobObject.getString("job_type")
                val jobLocation = jobObject.getString("job_location")
                val jobDescription = jobObject.getString("job_description")
                val workplaceType = jobObject.getString("workplace_type")
                val tags = jobObject.getJSONArray("tags").let { tagArray ->
                    (0 until tagArray.length()).map { tagArray.getString(it) }
                }

                val job = Job(
                    jobId = jobId,
                    organizationId = organizationId,
                    jobTitle = jobTitle,
                    jobType = jobType,
                    jobLocation = jobLocation,
                    jobDescription = jobDescription,
                    workplaceType = workplaceType,
                    tags = tags
                )

                // Parse Organization data (if available)
                val organizationObject = jobObject.optJSONObject("organization")
                val organization = if (organizationObject != null) {
                    OrganizationData(
                        organizationId = organizationObject.optString("organization_id", ""),
                        organizationName = organizationObject.optString("organization_name", "Unknown Organization"),
                        organizationLogo = organizationObject.optString("organization_logo", null),
                        organizationLocation = organizationObject.optString("organization_location", null)
                    )
                } else {
                    OrganizationData(
                        organizationId = organizationId,
                        organizationName = "Unknown Organization",
                        organizationLogo = null,
                        organizationLocation = null
                    )
                }

                // Combine Job and Organization into a wrapper object
                val jobWithOrg = JobWithOrganization(job, organization)
                jobWithOrgList.add(jobWithOrg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error parsing job data", Toast.LENGTH_SHORT).show()
        }
    }
}