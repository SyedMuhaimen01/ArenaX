package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

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
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class viewEventDetails : AppCompatActivity() {
    private lateinit var organizationLogo: ImageView
    private lateinit var organizationName: TextView
    private lateinit var organizationLocation: TextView
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

    private lateinit var requestQueue: RequestQueue
    private var eventId: String? = null
    private var organizationId: String? = null

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
            Toast.makeText(this, "Interest shown for $eventId", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        organizationLogo = findViewById(R.id.profilePicture)
        organizationName = findViewById(R.id.organizationNameTextView)
        organizationLocation = findViewById(R.id.locationTextView)
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
    }

    private fun setEventDetailsFromIntent() {
        val bannerUrl = intent.getStringExtra("eventBanner") ?: ""

        eventName.text = intent.getStringExtra("eventName") ?: "N/A"
        gameName.text = intent.getStringExtra("gameName") ?: "N/A"
        location.text = intent.getStringExtra("eventLocation") ?: "Unknown"
        eventMode.text = intent.getStringExtra("eventMode") ?: "N/A"
        platform.text = intent.getStringExtra("eventPlatform") ?: "N/A"
        eventDescription.text = intent.getStringExtra("eventDetails") ?: "No description available"
        startDate.text = intent.getStringExtra("startDate") ?: "N/A"
        endDate.text = intent.getStringExtra("endDate") ?: "N/A"
        startTime.text = intent.getStringExtra("startTime") ?: "N/A"
        endTime.text = intent.getStringExtra("endTime") ?: "N/A"
        eventLink.text = intent.getStringExtra("eventLink") ?: "N/A"

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

                organizationName.text = orgName
                organizationLocation.text = orgLocation
                if (orgLogo.isNotEmpty()) {
                    Glide.with(this).load(orgLogo).placeholder(R.drawable.battlegrounds_icon_background).into(organizationLogo)
                } else {
                    organizationLogo.setImageResource(R.drawable.battlegrounds_icon_background)
                }
            },
            { error ->
                Toast.makeText(this, "Error fetching organization: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(jsonObjectRequest)
    }
}
