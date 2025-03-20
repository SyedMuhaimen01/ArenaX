package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

import android.content.Intent
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
import com.muhaimen.arenax.utils.Constants
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.dataClasses.esportsNotificationData
import com.muhaimen.arenax.esportsManagement.battlegrounds.battlegrounds
import com.muhaimen.arenax.utils.FirebaseManager
import java.text.SimpleDateFormat
import java.util.Locale

class viewEventDetails : AppCompatActivity() {
    private lateinit var organizationNameTextView: TextView
    private lateinit var organizationLocationTextView: TextView
    private lateinit var organizationWebsiteTextView: TextView
    private lateinit var organizationIndustryTextView: TextView
    private lateinit var organizationTypeTextView: TextView
    private lateinit var organizationSizeTextView: TextView
    private lateinit var organizationTaglineTextView: TextView
    private lateinit var organizationLogoImageView: ImageView
    private lateinit var eventBanner: ImageView
    private lateinit var eventName: TextView
    private lateinit var eventMode: TextView
    private lateinit var platform: TextView
    private lateinit var location: TextView
    private lateinit var eventDescription: TextView
    private lateinit var startDate: TextView
    private lateinit var endDate: TextView
    private lateinit var startTime: TextView
    private lateinit var endTime: TextView
    private lateinit var eventLink: TextView
    private lateinit var showInterestButton: Button
    private lateinit var gameName: TextView
    private lateinit var loadedFromStatus:String

    private lateinit var requestQueue: RequestQueue
    private var eventId: String? = null
    private var organizationId: String? = null
    private var organizationName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_event_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)
        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)

        initializeViews()
        requestQueue = Volley.newRequestQueue(this)

        // Receiving intent data
        eventId = intent.getStringExtra("eventID")
        organizationId = intent.getStringExtra("organizationId")
        // Set event details
        setEventDetailsFromIntent()

        // Fetch organization details
        if (!organizationId.isNullOrEmpty()) {
            fetchOrganizationDetails(organizationId!!)
        }

        showInterestButton.setOnClickListener {
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

                        // Step 3: Prepare the event details
                        val event = eventName.text.toString()
                        val organizationName = organizationNameTextView.text.toString()

                        // Step 4: Check if the user has already shown interest in this event
                        val userNotificationsRef = FirebaseDatabase.getInstance().getReference("userData")
                            .child(currentUserId)
                            .child("esportsNotifications")
                            .child("applications")

                        val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")
                        val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(organizationName)
                        val userNotificationId = userNotificationsRef.push().key ?: return
                        val orgNotificationId = organizationsRef.push().key ?: return
                        val query = userNotificationsRef.orderByChild("eventId").equalTo(eventId)

                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var alreadyApplied = false

                                // Iterate through the existing notifications
                                for (notificationSnapshot in snapshot.children) {
                                    val notification = notificationSnapshot.getValue(esportsNotificationData::class.java)
                                    if (notification != null &&
                                        notification.userId == currentUserId &&
                                        notification.organizationName == organizationName &&
                                        notification.eventId == eventId
                                    ) {
                                        alreadyApplied = true
                                        break
                                    }
                                }

                                if (alreadyApplied) {
                                    // Step 5a: If already applied, show a message and navigate to battlegrounds
                                    Toast.makeText(
                                        this@viewEventDetails,
                                        "You have already shown interest in this event",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this@viewEventDetails, battlegrounds::class.java)
                                    startActivity(intent)
                                } else {
                                    // Step 5b: If not applied, construct the notification content dynamically
                                    val notificationContent = "$userName showed interest in Event: $event"

                                    // Step 6: Create the notification object
                                    val notificationObject1 = esportsNotificationData(
                                        notificationId = userNotificationId ,
                                        userId = currentUserId,
                                        content = notificationContent,
                                        organizationName = organizationName,
                                        eventId = eventId ?: ""
                                    )
                                    val notificationObject2 = esportsNotificationData(
                                        notificationId = orgNotificationId,
                                        userId = currentUserId,
                                        content = notificationContent,
                                        organizationName = organizationName,
                                        eventId = eventId ?: ""
                                    )

                                    // Step 7: Save the notification to Firebase (userData node)
                                    userNotificationsRef.child(userNotificationId).setValue(notificationObject1)
                                        .addOnSuccessListener {
                                            // Step 8: Query the organizationsData node to find the orgId
                                            orgQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(orgSnapshot: DataSnapshot) {
                                                    var orgId: String? = null

                                                    // Find the orgId based on the organizationName
                                                    for (org in orgSnapshot.children) {
                                                        orgId = org.key
                                                        break // Assuming organization names are unique
                                                    }

                                                    if (orgId.isNullOrEmpty()) {
                                                        Toast.makeText(
                                                            this@viewEventDetails,
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
                                                                this@viewEventDetails,
                                                                "Interest shown successfully",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            // Navigate to battlegrounds activity
                                                            val intent = Intent(
                                                                this@viewEventDetails,
                                                                battlegrounds::class.java
                                                            )
                                                            startActivity(intent)
                                                        }
                                                        .addOnFailureListener { error ->
                                                            Toast.makeText(
                                                                this@viewEventDetails,
                                                                "Failed to save notification for organization: ${error.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Toast.makeText(
                                                        this@viewEventDetails,
                                                        "Database error while fetching organization data: ${error.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })
                                        }
                                        .addOnFailureListener { error ->
                                            Toast.makeText(
                                                this@viewEventDetails,
                                                "Failed to save notification for user: ${error.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@viewEventDetails,
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
        eventBanner = findViewById(R.id.bannerImageView)
        eventName = findViewById(R.id.eventNameTextView)
        gameName = findViewById(R.id.eventGameTextView)
        eventMode = findViewById(R.id.eventModeTextView)
        platform = findViewById(R.id.eventPlatformTextView)
        location = findViewById(R.id.eventLocationTextView)
        eventDescription = findViewById(R.id.eventDetailsTextView)
        startDate = findViewById(R.id.startDate)
        endDate = findViewById(R.id.endDate)
        startTime = findViewById(R.id.startTime)
        endTime = findViewById(R.id.endTime)
        eventLink = findViewById(R.id.eventLinkTextView)
        showInterestButton = findViewById(R.id.showInterestButton)

        organizationNameTextView = findViewById(R.id.organizationNameTextView)
        organizationLocationTextView = findViewById(R.id.organizationLocationTextView)
        organizationIndustryTextView = findViewById(R.id.organizationIndustryTextView)
        organizationWebsiteTextView = findViewById(R.id.organizationWebsiteTextView)
        organizationTypeTextView = findViewById(R.id.organizationTypeTextView)
        organizationSizeTextView = findViewById(R.id.organizationSizeTextView)
        organizationTaglineTextView = findViewById(R.id.organizationTaglineTextView)
        organizationLogoImageView = findViewById(R.id.profilePicture)
    }

    private fun formatDate(inputDate: String): String {
        return try {
            // Define the input date format (ISO 8601 format)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.isLenient = false

            // Parse the input date string into a Date object
            val date = inputFormat.parse(inputDate)

            // Define the output date format (day/month/year)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            // Format the Date object into the desired output format
            outputFormat.format(date ?: return "N/A")
        } catch (e: Exception) {
            // Handle parsing errors and return "N/A" if something goes wrong
            e.printStackTrace()
            "N/A"
        }
    }

    private fun setEventDetailsFromIntent() {
        val bannerUrl = intent.getStringExtra("eventBanner") ?: ""

        eventName.text = intent.getStringExtra("eventName") ?: "N/A"
        gameName.text = intent.getStringExtra("gameName") ?: "N/A"
        location.text = intent.getStringExtra("eventLocation") ?: "Unknown"
        eventMode.text = intent.getStringExtra("eventMode") ?: "N/A"
        platform.text = intent.getStringExtra("eventPlatform") ?: "N/A"
        eventDescription.text = intent.getStringExtra("eventDetails") ?: "No description available"
        val start = intent.getStringExtra("startDate") ?: "N/A"
        val end = intent.getStringExtra("endDate") ?: "N/A"
        val formattedStartDate = formatDate(start)
        val formattedEndDate = formatDate(end)
        startDate.text = formattedStartDate
        endDate.text = formattedEndDate
        startTime.text = intent.getStringExtra("startTime") ?: "N/A"
        endTime.text = intent.getStringExtra("endTime") ?: "N/A"
        eventLink.text = intent.getStringExtra("eventLink") ?: "N/A"
        loadedFromStatus = intent.getStringExtra("loadedFrom") ?: "N/A"
        if(loadedFromStatus == "ownOrganization"){
            showInterestButton.visibility = View.GONE
        }
        // Load event banner image
        Glide.with(this)
            .load(bannerUrl)
            .placeholder(R.drawable.battlegrounds_icon_background)
            .into(eventBanner)
    }


    private fun fetchOrganizationDetails(organizationId: String) {
        val url = "${Constants.SERVER_URL}manageEvents/fetchOrganization"

        val requestBody = JSONObject().apply {
            put("organization_id", organizationId)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                Log.d("Organization", "Raw JSON Response: $response")
                val orgName = response.optString("organization_name", "N/A")
                val orgLocation = response.optString("organization_location", "Unknown")
                val orgLogo = response.optString("organization_logo", "")
                Log.d("Organization", "Name: $orgName, Location: $orgLocation, Logo: $orgLogo")

                organizationNameTextView.text = orgName
                organizationLocationTextView.text = orgLocation
                organizationWebsiteTextView.text = response.optString("organization_website", "N/A")
                organizationIndustryTextView.text = response.optString("organization_industry", "N/A")
                organizationTypeTextView.text = response.optString("organization_type", "N/A")
                organizationSizeTextView.text = response.optString("organization_size", "N/A")
                organizationTaglineTextView.text = response.optString("organization_tagline", "N/A")

                if (orgLogo.isNotEmpty()) {
                    Glide.with(this).load(orgLogo).placeholder(R.drawable.battlegrounds_icon_background).into(organizationLogoImageView)
                } else {
                    organizationLogoImageView.setImageResource(R.drawable.battlegrounds_icon_background)
                }
            },
            { error ->
                Toast.makeText(this, "Error fetching organization: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(jsonObjectRequest)
    }

}
