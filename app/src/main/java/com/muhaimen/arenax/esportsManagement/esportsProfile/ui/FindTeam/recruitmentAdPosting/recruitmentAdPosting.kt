package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam.recruitmentAdPosting

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.esportsManagement.esportsProfile.esportsProfile
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class recruitmentAdPosting : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var jobTitle: EditText
    private lateinit var jobType: Spinner
    private lateinit var jobLocation: EditText
    private lateinit var jobDescription: EditText
    private lateinit var workplaceType: Spinner
    private lateinit var jobTag1: EditText
    private lateinit var jobTag2: EditText
    private lateinit var jobTag3: EditText
    private lateinit var jobTag4: EditText
    private lateinit var postJobButton: Button
    private lateinit var jobItem: Job
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recruitment_ad_posting)

        // Handle system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)
        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        initializeViews()

        postJobButton.setOnClickListener {
            collectJobDetails()
            postJobToBackend()
        }

        backButton.setOnClickListener {
            onBackPressed()
            finish()
        }
    }

    private fun initializeViews() {
        jobTitle = findViewById(R.id.jobTitleEditText)
        jobType = findViewById(R.id.jobTypeSpinner)
        jobLocation = findViewById(R.id.jobLocationEditText)
        jobDescription = findViewById(R.id.jobDescriptionEditText)
        workplaceType = findViewById(R.id.workplaceTypeSpinner)
        jobTag1 = findViewById(R.id.tag1)
        jobTag2 = findViewById(R.id.tag2)
        jobTag3 = findViewById(R.id.tag3)
        jobTag4 = findViewById(R.id.tag4)
        postJobButton = findViewById(R.id.postJobButton)
        backButton = findViewById(R.id.backButton)

        populateSpinners()
    }

    private fun populateSpinners() {
        // Set adapter for Job Type
        ArrayAdapter.createFromResource(
            this,
            R.array.job_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            jobType.adapter = adapter
        }

        // Set adapter for Workplace Type
        ArrayAdapter.createFromResource(
            this,
            R.array.workplace_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            workplaceType.adapter = adapter
        }
    }

    private fun collectJobDetails() {
        jobItem = Job(
            jobId = "",
            organizationId = "",
            jobTitle = jobTitle.text.toString(),
            jobType = jobType.selectedItem.toString(),
            jobLocation = jobLocation.text.toString(),
            jobDescription = jobDescription.text.toString(),
            workplaceType = workplaceType.selectedItem.toString(),
            tags = listOf(
                jobTag1.text.toString(),
                jobTag2.text.toString(),
                jobTag3.text.toString(),
                jobTag4.text.toString()
            )
        )
    }

    private fun postJobToBackend() {
        // Create the request body
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("jobTitle", jobItem.jobTitle)
            put("jobType", jobItem.jobType)
            put("jobLocation", jobItem.jobLocation)
            put("jobDescription", jobItem.jobDescription)
            put("workplaceType", jobItem.workplaceType)
            put("tags", JSONArray(jobItem.tags))
        }

        // Create the Volley request
        val request = object : JsonObjectRequest(
            Request.Method.POST,
            "${Constants.SERVER_URL}manageUserJobs/addRecruitmentAd",
            requestBody,
            { response ->
                // Handle successful response
                Toast.makeText(this, "Recruitment Ad Posted Successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, esportsProfile::class.java)
                startActivity(intent)
            },
            { error ->
                // Handle error response
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            // Extend the timeout duration and disable retries
            override fun getRetryPolicy(): RetryPolicy {
                return DefaultRetryPolicy(
                    15000, // Timeout in milliseconds (15 seconds)
                    0,     // Disable retries (0 means no retry)
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }
        }

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(request)
    }
}
