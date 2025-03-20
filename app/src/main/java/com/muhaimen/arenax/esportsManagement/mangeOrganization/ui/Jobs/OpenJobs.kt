package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

import android.content.Intent
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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.dataClasses.JobWithOrganization
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.esportsManagement.talentExchange.OrganizationsAdapter
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class OpenJobs : Fragment() {

    private lateinit var openJobsRecyclerView: RecyclerView
    private lateinit var openJobsAdapter: OpenJobsAdapter
    private lateinit var searchBar: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchButton: ImageButton
    private var jobWithOrgList: MutableList<JobWithOrganization> = mutableListOf()
    private lateinit var organizationName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_open_jobs, container, false)

        // Initialize UI elements
        openJobsRecyclerView = view.findViewById(R.id.openJobs_recyclerview)
        searchBar = view.findViewById(R.id.searchbar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        searchButton = view.findViewById(R.id.searchButton)
        organizationName = arguments?.getString("organization_name") ?: ""

        // Set up RecyclerViews
        openJobsRecyclerView.layoutManager = LinearLayoutManager(context)
        openJobsAdapter = OpenJobsAdapter(jobWithOrgList)
        openJobsRecyclerView.adapter = openJobsAdapter

        // Fetch initial data
        fetchOpenJobs()

        // Set up search functionality
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && searchBar.text.toString().trim().isEmpty()) {
               fetchOpenJobs() // Repopulate with original data
                openJobsRecyclerView.visibility = View.VISIBLE
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    fetchOpenJobs()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        searchButton.setOnClickListener {
            val searchText = searchBar.text.toString().trim()
            if (searchText.isNotEmpty()) {
                searchOpenJobs(searchText)
            }else{
                fetchOpenJobs()
            }
        }

        // Set up pull-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener {
            fetchOpenJobs()
            swipeRefreshLayout.isRefreshing = false
        }


        return view
    }

    private fun fetchOpenJobs() {
        val queue = Volley.newRequestQueue(context)
        val url = "${Constants.SERVER_URL}manageJobs/getOpenJobs"

        val requestBody = JSONObject().apply {
            put("organization_name", organizationName)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    Log.d("Volley", "Response: $response")
                    val jobsArray = response.getJSONArray("jobs")
                    clearAndPopulateAdapter(jobsArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error parsing job data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("Volley", "Error fetching open jobs: ${error.message}")
                Toast.makeText(context, "Error fetching open jobs", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(request)
    }

    private fun searchOpenJobs(searchText: String) {
        val queue = Volley.newRequestQueue(context)
        val url = "${Constants.SERVER_URL}manageJobs/searchOpenJobsByOrganization"

        val requestBody = JSONObject().apply {
            put("searchText", searchText)
            put("organizationName", organizationName)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    Log.d("Volley", "Response: $response")
                    val jobsArray = response.getJSONArray("jobs")
                    clearAndPopulateAdapter(jobsArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error parsing job data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("Volley", "Error searching open jobs: ${error.message}")
                Toast.makeText(context, "Error searching open jobs", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(request)
    }

    private fun clearAndPopulateAdapter(response: JSONArray) {

        jobWithOrgList.clear()

        // Parse the response and populate the list
        parseAndPopulateJobs(response)

        // Notify the appropriate adapter of the data change
        openJobsAdapter.updateData(jobWithOrgList)

    }

    private fun parseAndPopulateJobs(response: JSONArray) {
        try {
            for (i in 0 until response.length()) {
                val jobObject = response.getJSONObject(i)

                // Parse Job data
                val jobId = jobObject.optString("job_id", "")
                val organizationId = jobObject.optString("organization_id", "")
                val jobTitle = jobObject.optString("job_title", "")
                val jobType = jobObject.optString("job_type", "")
                val jobLocation = jobObject.optString("job_location", "")
                val jobDescription = jobObject.optString("job_description", "")
                val workplaceType = jobObject.optString("workplace_type", "")
                val tags = jobObject.getJSONArray("tags").let { tagArray ->
                    List(tagArray.length()) { index -> tagArray.optString(index, "") }
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