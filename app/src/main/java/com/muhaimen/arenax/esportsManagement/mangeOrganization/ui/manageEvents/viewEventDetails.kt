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
import com.muhaimen.arenax.dataClasses.Event

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
    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_event_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor,theme)
        window.navigationBarColor = resources.getColor(R.color.primaryColor,theme)
        initializeViews()
        var organizationId:String =" "
        intent.getParcelableExtra<Event>("event")?.let {
            event = it
            organizationId=event.organizationId
            eventName.text = event.eventName
            eventMode.text = event.eventMode
            platform.text = event.platform
            location.text = event.location
            eventDescription.text = event.eventDescription
            startDate.text = event.startDate
            endDate.text = event.endDate
            startTime.text = event.startTime
            endTime.text = event.endTime
            eventLink.text = event.eventLink

            Glide.with(this)
                .load(event.eventBanner)
                .placeholder(R.drawable.battlegrounds_icon_background)
                .into(eventBanner)
        }
        getOrganizationData(organizationId)

        showInterestButton.setOnClickListener {
            // show interest in event
        }
    }

    fun getOrganizationData(organizationId:String) {
        // get organization Data from db

    }

    fun initializeViews(){
        organizationLogo = findViewById(R.id.organizationLogo)
        organizationName = findViewById(R.id.organizationName)
        organizationLocation = findViewById(R.id.organizationLocationTextView)
        eventBanner = findViewById(R.id.bannerImageView)
        eventName = findViewById(R.id.eventNameTextView)
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
}