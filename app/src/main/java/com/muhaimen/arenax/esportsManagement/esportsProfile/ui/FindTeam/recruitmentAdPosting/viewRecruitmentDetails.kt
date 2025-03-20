package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam.recruitmentAdPosting

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
            Log.d("RecruitButton", "Recruit button clicked")

            // Step 1: Get the gamerTag from the Intent
            val userGamerTag = intent.getStringExtra("GamerTag")
            if (userGamerTag.isNullOrEmpty()) {
                Toast.makeText(this, "GamerTag not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Step 2: Query the database to fetch the userId using the gamerTag
            val databaseReference = FirebaseDatabase.getInstance().reference
            val usersRef = databaseReference.child("userData")

            // Query to find the user with the given gamerTag
            val query = usersRef.orderByChild("gamerTag").equalTo(userGamerTag)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var currentUserId: String? = null

                    // Iterate through the results to extract the userId
                    for (userSnapshot in snapshot.children) {
                        currentUserId = userSnapshot.key // The key is the userId
                        break // Assuming gamerTags are unique, we can stop after the first match
                    }

                    if (currentUserId.isNullOrEmpty()) {
                        Toast.makeText(
                            this@viewRecruitmentDetails,
                            "User not found with GamerTag: $userGamerTag",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    Log.d("RecruitButton", "Found userId: $currentUserId for GamerTag: $userGamerTag")

                    // Step 3: Fetch the user's name from Firebase
                    usersRef.child(currentUserId!!).child("gamerTag")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val userName = userSnapshot.getValue(String::class.java)
                                if (userName.isNullOrEmpty()) {
                                    Toast.makeText(
                                        this@viewRecruitmentDetails,
                                        "Failed to fetch user details",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return
                                }

                                // Step 4: Prepare the job details
                                val job = jobTitleTextView.text.toString()
                                val orgName = intent.getStringExtra("OrganizationName")
                                val jobId = intent.getStringExtra("JobId")

                                // Step 5: Check if the user has already shown interest in this event
                                val userNotificationsRef = FirebaseDatabase.getInstance().getReference("userData")
                                    .child(currentUserId!!)
                                    .child("esportsNotifications")
                                    .child("invites")
                                val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")
                                val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(orgName)
                                val userNotificationId = userNotificationsRef.push().key ?: return
                                val orgNotificationId = organizationsRef.push().key ?: return
                                val query = FirebaseDatabase.getInstance().getReference("organizationsData")
                                    .child(intent.getStringExtra("OrganizationId").toString())
                                    .child("esportsNotifications")
                                    .child("invites")
                                    .orderByChild("eventId").equalTo(jobId)

                                query.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        var alreadyApplied = false
                                        // Iterate through the existing notifications
                                        for (notificationSnapshot in snapshot.children) {
                                            val notification = notificationSnapshot.getValue(
                                                esportsNotificationData::class.java
                                            )
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
                                            // Step 6a: If already applied, show a message and navigate to battlegrounds
                                            Toast.makeText(
                                                this@viewRecruitmentDetails,
                                                "You have already shown interest in this event",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(this@viewRecruitmentDetails, battlegrounds::class.java)
                                            startActivity(intent)
                                        } else {
                                            // Step 6b: If not applied, construct the notification content dynamically
                                            val notificationContent = "$orgName showed interest in Ad: $job"
                                            // Step 7: Create the notification object
                                            val notificationObject1 = esportsNotificationData(
                                                notificationId = userNotificationId,
                                                userId = currentUserId!!,
                                                content = notificationContent,
                                                organizationName = orgName.toString(),
                                                eventId = jobId ?: ""
                                            )
                                            val notificationObject2 = esportsNotificationData(
                                                notificationId = orgNotificationId,
                                                userId = currentUserId!!,
                                                content = notificationContent,
                                                organizationName = orgName.toString(),
                                                eventId = jobId ?: ""
                                            )
                                            // Step 8: Save the notification to Firebase (userData node)
                                            userNotificationsRef.child(userNotificationId).setValue(notificationObject1)
                                                .addOnSuccessListener {
                                                    // Step 9: Query the organizationsData node to find the orgId
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
                                                                    this@viewRecruitmentDetails,
                                                                    "Organization not found",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                return
                                                            }
                                                            // Construct the reference to the organizationsData node
                                                            val organizationNotificationsRef = FirebaseDatabase.getInstance()
                                                                .getReference("organizationsData")
                                                                .child(orgId!!)
                                                                .child("esportsNotifications")
                                                                .child("invites")
                                                            // Save the notification in the organizationsData node
                                                            organizationNotificationsRef.child(orgNotificationId)
                                                                .setValue(notificationObject2)
                                                                .addOnSuccessListener {
                                                                    // Show success toast
                                                                    Toast.makeText(
                                                                        this@viewRecruitmentDetails,
                                                                        "Interest shown successfully",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    // Navigate to battlegrounds activity
                                                                    val intent = Intent(
                                                                        this@viewRecruitmentDetails,
                                                                        battlegrounds::class.java
                                                                    )
                                                                    startActivity(intent)
                                                                }
                                                                .addOnFailureListener { error ->
                                                                    Toast.makeText(
                                                                        this@viewRecruitmentDetails,
                                                                        "Failed to save notification for organization: ${error.message}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {
                                                            Toast.makeText(
                                                                this@viewRecruitmentDetails,
                                                                "Database error while fetching organization data: ${error.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    })
                                                }
                                                .addOnFailureListener { error ->
                                                    Toast.makeText(
                                                        this@viewRecruitmentDetails,
                                                        "Failed to save notification for user: ${error.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(
                                            this@viewRecruitmentDetails,
                                            "Database error while checking existing notifications: ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle errors when fetching user data
                                Toast.makeText(
                                    this@viewRecruitmentDetails,
                                    "Database error while fetching user details: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@viewRecruitmentDetails,
                        "Database error while fetching userId: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
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
        val loadedFromStatus = intent.getStringExtra("loadedFrom") ?: "N/A"
        if(loadedFromStatus == "ownProfile"){
            recruitButton.visibility = View.GONE
        }
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
