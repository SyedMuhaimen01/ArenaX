package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity

class ClosedJobDetails : AppCompatActivity() {
    private lateinit var organizationNameTextView: TextView
    private lateinit var jobTitleTextView: TextView
    private lateinit var jobDescriptionTextView: TextView
    private lateinit var jobLocationTextView: TextView
    private lateinit var workplaceTypeTextView: TextView
    private lateinit var jobTypeTextView: TextView
    private lateinit var tag1: TextView
    private lateinit var tag2: TextView
    private lateinit var tag3: TextView
    private lateinit var tag4: TextView
    private lateinit var organizationLogo: ImageView
    private lateinit var organizationName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_closed_job_details)

        // Handle edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)
        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)

        // Initialize views
        initializeViews()

        // Retrieve data from intent extras
        val jobId = intent.getStringExtra("JobId") ?: "N/A"
        val jobTitle = intent.getStringExtra("JobTitle") ?: "N/A"
        val jobLocation = intent.getStringExtra("JobLocation") ?: "Location not specified"
        val jobType = intent.getStringExtra("JobType") ?: "Not specified"
        val workplaceType = intent.getStringExtra("WorkplaceType") ?: "Not specified"
        val jobDescription = intent.getStringExtra("JobDescription") ?: "No description available"
        val jobTags = intent.getStringArrayListExtra("JobTags") ?: arrayListOf()
        val organizationId = intent.getStringExtra("OrganizationId") ?: "N/A"
        organizationName = intent.getStringExtra("OrganizationName") ?: "Unknown Organization"
        val organizationLogoUrl = intent.getStringExtra("OrganizationLogo")
        val organizationLocation = intent.getStringExtra("OrganizationLocation") ?: "Location not specified"

        // Populate views with retrieved data
        organizationNameTextView.text = organizationName
        jobTitleTextView.text = jobTitle
        jobDescriptionTextView.text = jobDescription
        jobLocationTextView.text = jobLocation
        workplaceTypeTextView.text = workplaceType
        jobTypeTextView.text = jobType

        // Load organization logo using Glide
        if (!organizationLogoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(organizationLogoUrl)
                .circleCrop()
                .placeholder(R.drawable.battlegrounds_icon_background)
                .into(organizationLogo)
        } else {
            organizationLogo.setImageResource(R.drawable.battlegrounds_icon_background)
        }

        // Populate tags
        tag1.text = jobTags.getOrNull(0) ?: ""
        tag2.text = jobTags.getOrNull(1) ?: ""
        tag3.text = jobTags.getOrNull(2) ?: ""
        tag4.text = jobTags.getOrNull(3) ?: ""

    }

    private fun initializeViews() {
        organizationNameTextView = findViewById(R.id.organizationNameTextView)
        jobTitleTextView = findViewById(R.id.jobTitleTextView)
        jobDescriptionTextView = findViewById(R.id.jobDescriptionTextView)
        jobLocationTextView = findViewById(R.id.locationTextView)
        workplaceTypeTextView = findViewById(R.id.workplaceTypeTextView)
        jobTypeTextView = findViewById(R.id.jobTypeTextView)
        tag1 = findViewById(R.id.tag1)
        tag2 = findViewById(R.id.tag2)
        tag3 = findViewById(R.id.tag3)
        tag4 = findViewById(R.id.tag4)
        organizationLogo = findViewById(R.id.organizationLogo)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this, OrganizationHomePageActivity::class.java)
        intent.putExtra("organization_name", organizationName)
        startActivity(intent)
    }
}