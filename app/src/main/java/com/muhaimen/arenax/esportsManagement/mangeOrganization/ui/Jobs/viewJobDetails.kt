package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.esportsNotificationData
import com.muhaimen.arenax.esportsManagement.battlegrounds.battlegrounds
import com.muhaimen.arenax.utils.FirebaseManager

import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity


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
        val organizationName = intent.getStringExtra("OrganizationName") ?: "Unknown Organization"
        val organizationLogoUrl = intent.getStringExtra("OrganizationLogo")
        val organizationLocation = intent.getStringExtra("OrganizationLocation") ?: "Location not specified"
        val loadedFromStatus = intent.getStringExtra("loadedFrom") ?: "N/A"
        if(loadedFromStatus == "ownOrganization"){
            applyButton.visibility = View.GONE
        }

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

        // Set up apply button click listener
        applyButton.setOnClickListener {
            // Step 1: Get the current user's ID
            val currentUserId = FirebaseManager.getCurrentUserId()
            if (currentUserId.isNullOrEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Step 2: Fetch the user's name from Firebase
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.child("userData").child(currentUserId).child("gamerTag")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        val userName = userSnapshot.getValue(String::class.java)
                        if (userName.isNullOrEmpty()) {
                            return
                        }

                        // Step 3: Prepare the job details
                        val job = jobTitleTextView.text.toString()
                        val orgName = organizationNameTextView.text.toString()

                        // Step 4: Check if the user has already shown interest in this event
                        val userNotificationsRef = FirebaseDatabase.getInstance().getReference("userData")
                            .child(currentUserId)
                            .child("esportsNotifications")
                            .child("applications")

                        val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")
                        val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(orgName)
                        val userNotificationId = userNotificationsRef.push().key ?: return
                        val orgNotificationId = organizationsRef.push().key ?: return
                        val query = userNotificationsRef.orderByChild("eventId").equalTo(jobId)

                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var alreadyApplied = false

                                // Iterate through the existing notifications
                                for (notificationSnapshot in snapshot.children) {
                                    val notification = notificationSnapshot.getValue(
                                        esportsNotificationData::class.java)
                                    if (notification != null &&
                                        notification.userId == currentUserId &&
                                        notification.organizationName == orgName &&
                                        notification.eventId == jobId
                                    ) {
                                        alreadyApplied = true
                                        break
                                    }
                                }

                                if (alreadyApplied) {
                                    // Step 5a: If already applied, show a message and navigate to battlegrounds
                                    Toast.makeText(
                                        this@viewJobDetails,
                                        "You have already shown interest in this event",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this@viewJobDetails, battlegrounds::class.java)
                                    startActivity(intent)
                                } else {
                                    // Step 5b: If not applied, construct the notification content dynamically
                                    val notificationContent = "$userName showed interest in Job: $job"

                                    // Step 6: Create the notification object
                                    val notificationObject1 = esportsNotificationData(
                                        notificationId = userNotificationId ,
                                        userId = currentUserId,
                                        content = notificationContent,
                                        organizationName = orgName,
                                        eventId = jobId ?: ""
                                    )
                                    val notificationObject2 = esportsNotificationData(
                                        notificationId = orgNotificationId,
                                        userId = currentUserId,
                                        content = notificationContent,
                                        organizationName = orgName,
                                        eventId = jobId ?: ""
                                    )

                                    // Step 7: Save the notification to Firebase (userData node)
                                    userNotificationsRef.child(userNotificationId).setValue(notificationObject1)
                                        .addOnSuccessListener {
                                            // Step 8: Query the organizationsData node to find the orgId
                                            orgQuery.addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(orgSnapshot: DataSnapshot) {
                                                    var orgId: String? = null

                                                    // Find the orgId based on the organizationName
                                                    for (org in orgSnapshot.children) {
                                                        orgId = org.key
                                                        break // Assuming organization names are unique
                                                    }

                                                    if (orgId.isNullOrEmpty()) {
                                                        Toast.makeText(
                                                            this@viewJobDetails,
                                                            "Organization not found",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        return
                                                    }

                                                    // Construct the reference to the organizationsData node
                                                    val organizationNotificationsRef = FirebaseDatabase.getInstance()
                                                        .getReference("organizationsData")
                                                        .child(orgId)
                                                        .child("esportsNotifications")
                                                        .child("applications")

                                                    // Save the notification in the organizationsData node
                                                    organizationNotificationsRef.child(orgNotificationId)
                                                        .setValue(notificationObject2)
                                                        .addOnSuccessListener {
                                                            // Show success toast
                                                            Toast.makeText(
                                                                this@viewJobDetails,
                                                                "Interest shown successfully",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            // Navigate to battlegrounds activity
                                                            val intent = Intent(
                                                                this@viewJobDetails,
                                                                battlegrounds::class.java
                                                            )
                                                            startActivity(intent)
                                                        }
                                                        .addOnFailureListener { error ->
                                                            Toast.makeText(
                                                                this@viewJobDetails,
                                                                "Failed to save notification for organization: ${error.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Toast.makeText(
                                                        this@viewJobDetails,
                                                        "Database error while fetching organization data: ${error.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })
                                        }
                                        .addOnFailureListener { error ->
                                            Toast.makeText(
                                                this@viewJobDetails,
                                                "Failed to save notification for user: ${error.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@viewJobDetails,
                                    "Database error while checking existing notifications: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })

                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle errors when fetching user data

                    }
                })
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

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        super.onBackPressed()
    }
}