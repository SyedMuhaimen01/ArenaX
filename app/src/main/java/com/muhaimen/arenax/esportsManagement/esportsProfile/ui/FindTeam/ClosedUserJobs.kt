package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam

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
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.dataClasses.JobWithUserDetails
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.ClosedUserJobsAdapter
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class ClosedUserJobs : Fragment() {

    private lateinit var openJobsRecyclerView: RecyclerView
    private lateinit var openJobsAdapter: ClosedUserJobsAdapter
    private lateinit var searchBar: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchButton: ImageButton
    private var jobWithUserDetailsList: MutableList<JobWithUserDetails> = mutableListOf()
    private lateinit var firebaseUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_closed_user_jobs, container, false)

        // Initialize UI elements
        openJobsRecyclerView = view.findViewById(R.id.openJobs_recyclerview)
        searchBar = view.findViewById(R.id.searchbar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        searchButton = view.findViewById(R.id.searchButton)

        // Set up RecyclerViews
        openJobsRecyclerView.layoutManager = LinearLayoutManager(context)
        openJobsAdapter = ClosedUserJobsAdapter(jobWithUserDetailsList)
        openJobsRecyclerView.adapter = openJobsAdapter

        firebaseUid=FirebaseManager.getCurrentUserId().toString()
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
            } else {
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
        val url = "${Constants.SERVER_URL}manageUserJobs/closedRecruitmentAds/$firebaseUid"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    Log.d("FeTCHOPENVolley", "Response: $response")
                    val jobsArray = response.getJSONArray("jobAds")
                    clearAndPopulateAdapter(jobsArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                    //Toast.makeText(context, "Error parsing job data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("Volley", "Error fetching open jobs: ${error.message}")
                //Toast.makeText(context, "Error fetching open jobs", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    private fun searchOpenJobs(searchText: String) {
        val queue = Volley.newRequestQueue(context)
        val url = "${Constants.SERVER_URL}manageUserJobs/searchUserClosedJobs"

        val requestBody = JSONObject().apply {
            put("searchText", searchText)
            put("firebaseUid", firebaseUid)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    Log.d("FeTCHOPENVolley", "Response: $response")
                    val jobsArray = response.getJSONArray("jobAds")
                    clearAndPopulateAdapter(jobsArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                    //Toast.makeText(context, "Error parsing job data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("Volley", "Error searching open jobs: ${error.message}")
                //Toast.makeText(context, "Error searching open jobs", Toast.LENGTH_SHORT).show()
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
        jobWithUserDetailsList.clear()

        // Parse the response and populate the list
        parseAndPopulateJobs(response)
        //Log.d("JobWithUserDetails", jobWithUserDetailsList.toString())
        // Notify the adapter of the data change
        openJobsAdapter.updateData(jobWithUserDetailsList)
    }

    private fun parseAndPopulateJobs(response: JSONArray) {
        try {
            for (i in 0 until response.length()) {
                val jobObject = response.getJSONObject(i)

                // Parse Job data
                val jobId = jobObject.optString("jobId", "")
                val organizationId = jobObject.optString("userId", "")
                val jobTitle = jobObject.optString("jobTitle", "")
                val jobType = jobObject.optString("jobType", "")
                val jobLocation = jobObject.optString("jobLocation", "")
                val jobDescription = jobObject.optString("jobDescription", "")
                val workplaceType = jobObject.optString("workplaceType", "")
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

                // Parse User data
                val userDetailsObject = jobObject.optJSONObject("user_details")
                val user = if (userDetailsObject != null) {
                    UserData(
                        userId = userDetailsObject.optString("full_name", ""),
                        fullname = userDetailsObject.optString("full_name", "Unknown User"),
                        email = "", // Not provided in the API response
                        dOB = "", // Not provided in the API response
                        gamerTag = userDetailsObject.optString("gamer_tag", "No Gamer Tag"),
                        profilePicture = userDetailsObject.optString("profile_picture_url", null),
                        gender = Gender.PreferNotToSay, // Not provided in the API response
                        bio = "", // Not provided in the API response
                        location = "", // Not provided in the API response
                        accountVerified = false, // Not provided in the API response
                        playerId = "", // Not provided in the API response
                        rank = "" // Not provided in the API response
                    )
                } else {
                    UserData(
                        userId = "",
                        fullname = "Unknown User",
                        email = "",
                        dOB = "",
                        gamerTag = "No Gamer Tag",
                        profilePicture = null,
                        gender = Gender.PreferNotToSay,
                        bio = "",
                        location = "",
                        accountVerified = false,
                        playerId = "",
                        rank = ""
                    )
                }

                // Combine Job and User into a wrapper object
                val jobWithUserDetails = JobWithUserDetails(job, user)
                jobWithUserDetailsList.add(jobWithUserDetails)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error parsing job data", Toast.LENGTH_SHORT).show()
        }
    }
}