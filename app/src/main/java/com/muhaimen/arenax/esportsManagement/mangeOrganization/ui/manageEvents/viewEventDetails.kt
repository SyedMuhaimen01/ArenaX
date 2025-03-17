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
                organizationWebsiteTextView?.text = response.optString("organization_website", "N/A")
                organizationIndustryTextView?.text = response.optString("organization_industry", "N/A")
                organizationTypeTextView?.text = response.optString("organization_type", "N/A")
                organizationSizeTextView?.text = response.optString("organization_size", "N/A")
                organizationTaglineTextView?.text = response.optString("organization_tagline", "N/A")

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
