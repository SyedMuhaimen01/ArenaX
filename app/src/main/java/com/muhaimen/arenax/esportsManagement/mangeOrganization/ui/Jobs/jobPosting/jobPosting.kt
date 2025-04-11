package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.jobPosting

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class jobPosting : AppCompatActivity() {
    private lateinit var jobTitleEditText: EditText
    private lateinit var jobType: Spinner
    private lateinit var jobLocation: EditText
    private lateinit var jobDescription: EditText
    private lateinit var workplaceType: Spinner
    private lateinit var jobTag1: EditText
    private lateinit var jobTag2: EditText
    private lateinit var jobTag3: EditText
    private lateinit var jobTag4: EditText
    private lateinit var postJobButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var jobItem: Job
    private lateinit var organizationName: String
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_posting)

        // Set navigation and status bar colors
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        // Receive organization name from intent
        organizationName = intent.getStringExtra("organization_name") ?: ""

        database=FirebaseDatabase.getInstance()
        auth=FirebaseAuth.getInstance()
        userId=auth.currentUser?.uid.toString()
        initializeViews()

        postJobButton.setOnClickListener {
            // Collect user input and store in Job data class
            postJobButton.isEnabled = false
            if (validateFields()) {
                // Proceed with posting the job
                collectJobDetails()
                postJobToBackend()

            } else {
                postJobButton.isEnabled = true
                // Show a toast or log a message if validation fails
                Toast.makeText(this, "Please fix the errors in the form", Toast.LENGTH_SHORT).show()
            }

        }

        backButton.setOnClickListener {
            onBackPressed()
            finish()
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        // Validate Job Title
        if (jobTitleEditText.text.toString().trim().isEmpty()) {
            jobTitleEditText.error = "Job title is required"
            isValid = false
        }

        // Validate Job Location
        if (jobLocation.text.toString().trim().isEmpty()) {
            jobLocation.error = "Job location is required"
            isValid = false
        }

        // Validate Job Description
        if (jobDescription.text.toString().trim().isEmpty()) {
            jobDescription.error = "Job description is required"
            isValid = false
        }

        return isValid
    }
    private fun initializeViews() {
        jobTitleEditText = findViewById(R.id.jobTitleEditText)
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
            R.layout.dropdown_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.dropdown_spinner_item)
            jobType.adapter = adapter
        }

        // Set adapter for Workplace Type
        ArrayAdapter.createFromResource(
            this,
            R.array.workplace_types,
            R.layout.dropdown_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.dropdown_spinner_item)
            workplaceType.adapter = adapter
        }
    }

    private fun collectJobDetails() {
        jobItem = Job(
            jobId = "",
            organizationId = "",
            jobTitle = jobTitleEditText.text.toString(),
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
            put("organization_name", organizationName)
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
            "${Constants.SERVER_URL}manageJobs/addJob",
            requestBody,
            { response ->
                postJobButton.isEnabled = true
                // Handle successful response
                Toast.makeText(this, "Job Posted Successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to the OrganizationHomePageActivity
                val intent = Intent(this, OrganizationHomePageActivity::class.java)
                intent.putExtra("organization_name", organizationName)
                startActivity(intent)
            },
            { error ->
                postJobButton.isEnabled = true
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
