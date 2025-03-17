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
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class EmployeesFragment : Fragment() {

    private lateinit var employeesAdapter: EmployeesAdapter
    private lateinit var recyclerView: RecyclerView
    private val jobList: MutableList<Job> = mutableListOf()
    private lateinit var firebaseUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_employees, container, false)

        // Set up RecyclerView and Adapter
        recyclerView = rootView.findViewById(R.id.employees_recyclerview)
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(activity)
        employeesAdapter = EmployeesAdapter(jobList)
        recyclerView.adapter = employeesAdapter
        firebaseUid = FirebaseManager.getCurrentUserId().toString()
        // Fetch job availability data
        fetchJobAvailabilityData(firebaseUid)

        return rootView
    }

    private fun fetchJobAvailabilityData(firebaseUid: String) {
        val url = "${Constants.SERVER_URL}manageTalentXchange/fetchJobAvailability/$firebaseUid"

        // Create a Volley request queue
        val requestQueue = Volley.newRequestQueue(requireContext())

        // Create a JSON array request
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                // Parse the response and populate the job list
                parseJobAvailabilityResponse(response)
                Log.d("EmployeesFragment", "Job data $response")
            },
            { error ->
                Log.e("EmployeesFragment", "Error fetching job data: $error")
                Toast.makeText(activity, "Error fetching job data", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the request queue
        requestQueue.add(jsonArrayRequest)
    }

    private fun searchJobsData(searchText: String) {
        val url = "${Constants.SERVER_URL}manageTalentXchange/searchUserJobAvailability"

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
                    val jobs = response.getJSONArray("jobs")
                    Log.d("EmployeesFragment 1122", "Job data $jobs")
                    parseJobAvailabilityResponse(jobs)
                }
            },
            { error ->
                Log.e("EmployeesFragment", "Error fetching job data: ${error.message}")
                Log.e("EmployeesFragment", "Error details: ${error.cause}")
                Toast.makeText(activity, "Error fetching job data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the Volley request queue
        Volley.newRequestQueue(context).add(request)
    }

    private fun parseJobAvailabilityResponse(response: JSONArray) {
        jobList.clear() // Clear existing jobs

        for (i in 0 until response.length()) {
            val jobObject = response.getJSONObject(i)

            // Safely parse job fields with default values if missing
            val jobId = jobObject.optString("jobId", "Unknown")  // Default to "Unknown" if jobId is missing
            val organizationId = jobObject.optString("organizationId", "Unknown")
            val jobTitle = jobObject.optString("jobTitle", "Unknown")
            val jobType = jobObject.optString("jobType", "Unknown")
            val jobLocation = jobObject.optString("jobLocation", "Unknown")
            val jobDescription = jobObject.optString("jobDescription", "No description available")
            val workplaceType = jobObject.optString("workplaceType", "Unknown")

            // Parse tags
            val tags = mutableListOf<String>()
            val tagsArray = jobObject.optJSONArray("tags") ?: JSONArray()  // Avoid null pointer if tags are missing
            for (j in 0 until tagsArray.length()) {
                tags.add(tagsArray.getString(j))
            }

            // Create a Job object
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
            // Add job to the list
            jobList.add(job)

        }

        // Notify the adapter that the data has changed
        employeesAdapter.updateJobList(jobList)
        employeesAdapter.notifyDataSetChanged()
    }
}

