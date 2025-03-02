package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam.recruitmentAdPosting

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R

class viewRecruitmentDetails : AppCompatActivity() {
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
    private lateinit var recruitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_recruitment_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)
        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)

        initializeViews()
        loadDataFromIntent()

        recruitButton.setOnClickListener {
            Toast.makeText(this, "Application Submitted!", Toast.LENGTH_SHORT).show()
            // Implement job application logic here
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
        recruitButton = findViewById(R.id.recruitButton)
    }

    private fun loadDataFromIntent() {
        userFullNameTextView.text = intent.getStringExtra("FullName") ?: "Unknown"
        gamertagTextView.text = intent.getStringExtra("GamerTag") ?: "No Gamertag"
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
