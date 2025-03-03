package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R

class viewJobDetails : AppCompatActivity() {
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
    private lateinit var applyButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_job_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)
        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)

        initializeViews()


        organizationNameTextView.text = intent.getStringExtra("OrganizationName") ?: "Unknown"
        jobTitleTextView.text = intent.getStringExtra("JobTitle") ?: "N/A"
        jobDescriptionTextView.text = intent.getStringExtra("JobDescription") ?: "No description available"
        jobLocationTextView.text = intent.getStringExtra("JobLocation") ?: "Location not specified"
        workplaceTypeTextView.text = intent.getStringExtra("WorkplaceType") ?: "Not specified"
        jobTypeTextView.text = intent.getStringExtra("JobType") ?: "Not specified"

        val logoUrl = intent.getStringExtra("OrganizationLogoUrl")
        if (!logoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(logoUrl)
                .circleCrop()
                .placeholder(R.drawable.battlegrounds_icon_background) // Placeholder image
                .into(organizationLogo)
        } else {
            organizationLogo.setImageResource(R.drawable.battlegrounds_icon_background)
        }

        val jobTags = intent.getStringArrayListExtra("JobTags") ?: arrayListOf()
        tag1.text = jobTags.getOrNull(0) ?: ""
        tag2.text = jobTags.getOrNull(1) ?: ""
        tag3.text = jobTags.getOrNull(2) ?: ""
        tag4.text = jobTags.getOrNull(3) ?: ""

        applyButton.setOnClickListener {
            // Implement job application logic here
        }
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
        applyButton = findViewById(R.id.applyButton)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
