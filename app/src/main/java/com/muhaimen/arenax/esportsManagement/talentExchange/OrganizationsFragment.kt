package com.muhaimen.arenax.esportsManagement.talentExchange

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Event
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONObject

class OrganizationsFragment : Fragment() {

    private lateinit var organizationsAdapter: OrganizationsAdapter
    private lateinit var recyclerView: RecyclerView
    private var jobList: MutableList<Job> = mutableListOf()
    private lateinit var firebaseUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organizations, container, false)

        recyclerView = view.findViewById(R.id.organizations_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Initialize the adapter
        organizationsAdapter = OrganizationsAdapter(jobList)
        recyclerView.adapter = organizationsAdapter
        firebaseUid= FirebaseManager.getCurrentUserId().toString()
        fetchJobsData(firebaseUid)

        return view
    }

    private fun fetchJobsData(firebaseUid: String) {
        val url = "${Constants.SERVER_URL}manageTalentXchange/fetchJobs/$firebaseUid"

        // Create a Volley request to fetch the job data
        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,  // No body needed for GET request
            { response ->
                // Handle the response
                parseAndPopulateJobs(response)
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

    private fun searchJobsData(searchText: String) {
        val url = "${Constants.SERVER_URL}manageTalentXchange/searchJobs"

        // Create a JSONObject with the search text as body for the POST request
        val requestBody = JSONObject()
        requestBody.put("searchText", searchText)

        // Create a Volley request to search jobs
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,  // Request body with search text
            { response ->
                // Handle the response
                if (response.has("error")) {
                    // Handle error message from the response
                    Log.e("Volley", "Error: ${response.getString("error")}")
                    Toast.makeText(context, "Error: ${response.getString("error")}", Toast.LENGTH_SHORT).show()
                } else {
                    // Process the job data returned in the response
                    val jobs = response.getJSONArray("jobs") // Assuming "jobs" is the key in response
                    parseAndPopulateJobs(jobs)
                }
            },
            { error ->
                // Handle error
                Log.e("Volley", "Error searching jobs data: ${error.message}")
                Toast.makeText(context, "Error searching jobs data", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the Volley request queue
        Volley.newRequestQueue(context).add(request)
    }

    private fun parseAndPopulateJobs(response: org.json.JSONArray) {
        val jobList = mutableListOf<Job>()  // Create a list to hold parsed jobs

        try {
            // Loop through the JSON array and parse each job
            for (i in 0 until response.length()) {
                val jobObject = response.getJSONObject(i)

                // Extract data from the jobObject and map it to the Job data class
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

                // Create a Job object
                val job = Job(
                    jobId,
                    organizationId,
                    jobTitle,
                    jobType,
                    jobLocation,
                    jobDescription,
                    workplaceType,
                    tags
                )

                // Add to the list
                jobList.add(job)
            }

            // Now, update the adapter with the job list
            organizationsAdapter.updateJobsList(jobList)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error parsing job data", Toast.LENGTH_SHORT).show()
        }
    }

}