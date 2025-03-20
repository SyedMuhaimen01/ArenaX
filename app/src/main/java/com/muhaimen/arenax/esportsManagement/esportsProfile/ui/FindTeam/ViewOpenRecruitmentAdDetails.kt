package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.esportsProfile.esportsProfile
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

class ViewOpenRecruitmentAdDetails : AppCompatActivity() {
    private lateinit var userFullNameTextView: TextView
    private lateinit var gamertagTextView: TextView
    private lateinit var jobTitleTextView: TextView
    private lateinit var jobDescriptionTextView: TextView
    private lateinit var jobLocationTextView: TextView
    private lateinit var workplaceTypeTextView: TextView
    private lateinit var jobTypeTextView: TextView
    private lateinit var tag1: TextView
    private lateinit var tag2: TextView
    private lateinit var tag3: TextView
    private lateinit var tag4: TextView
    private lateinit var userProfilePicture: ImageView
    private lateinit var closeButton: Button
    private lateinit var jobId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_veiw_open_recruitment_ad_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)
        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)

        initializeViews()
        jobId = intent.getStringExtra("JobId") ?: ""
        userFullNameTextView.text = intent.getStringExtra("FullName") ?: "Unknown"
        gamertagTextView.text = intent.getStringExtra("GamerTag") ?: "No Gamertag"
        jobTitleTextView.text = intent.getStringExtra("JobTitle") ?: "N/A"
        jobDescriptionTextView.text = intent.getStringExtra("JobDescription") ?: "No description available"
        jobLocationTextView.text = intent.getStringExtra("JobLocation") ?: "Location not specified"
        workplaceTypeTextView.text = intent.getStringExtra("WorkplaceType") ?: "Not specified"
        jobTypeTextView.text = intent.getStringExtra("JobType") ?: "Not specified"

        val logoUrl = intent.getStringExtra("ProfilePictureUrl")
        if (!logoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(logoUrl)
                .circleCrop()
                .placeholder(R.drawable.battlegrounds_icon_background)
                .into(userProfilePicture)
        } else {
            userProfilePicture.setImageResource(R.drawable.battlegrounds_icon_background)
        }

        val jobTags = intent.getStringArrayListExtra("JobTags") ?: arrayListOf()
        tag1.text = jobTags.getOrNull(0) ?: ""
        tag2.text = jobTags.getOrNull(1) ?: ""
        tag3.text = jobTags.getOrNull(2) ?: ""
        tag4.text = jobTags.getOrNull(3) ?: ""

        closeButton.setOnClickListener {
            closeJob(jobId)
        }
    }

    private fun initializeViews() {
        userFullNameTextView = findViewById(R.id.fullNameTextView)
        gamertagTextView = findViewById(R.id.gamerTagTextView)
        jobTitleTextView = findViewById(R.id.jobTitleTextView)
        jobDescriptionTextView = findViewById(R.id.recruitmentDetailsTextView)
        jobLocationTextView = findViewById(R.id.locationTextView)
        workplaceTypeTextView = findViewById(R.id.workplaceTypeTextView)
        jobTypeTextView = findViewById(R.id.jobTypeTextView)
        tag1 = findViewById(R.id.tag1)
        tag2 = findViewById(R.id.tag2)
        tag3 = findViewById(R.id.tag3)
        tag4 = findViewById(R.id.tag4)
        userProfilePicture = findViewById(R.id.profilePicture)
        closeButton = findViewById(R.id.closeButton)
    }


    private fun closeJob(jobId: String) {
        // Step 1: Initialize Volley request queue
        val queue = Volley.newRequestQueue(this)
        val url = "${Constants.SERVER_URL}manageUserJobs/closeRecruitmentAd"

        // Step 2: Create JSON payload for the request
        Log.d("sendddJobId", jobId)
        val jsonBody = JSONObject().apply {
            put("jobId", jobId)
        }

        // Step 3: Create POST request
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            { response ->
                // Handle success response
                Toast.makeText(this, "Recruitment successfully closed", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, esportsProfile::class.java)
                startActivity(intent)
            },
            { error ->
                // Handle error response
                Toast.makeText(this, "Failed to close job: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        // Step 4: Add the request to the queue
        queue.add(jsonObjectRequest)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
