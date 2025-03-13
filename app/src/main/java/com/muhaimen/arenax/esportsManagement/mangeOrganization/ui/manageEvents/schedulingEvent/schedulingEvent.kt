package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents.schedulingEvent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Event

class schedulingEvent : AppCompatActivity() {
    private lateinit var eventBanner: ImageView
    private lateinit var eventName: EditText
    private lateinit var eventMode: Spinner
    private lateinit var platform: Spinner
    private lateinit var location: EditText
    private lateinit var eventDescription: EditText
    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var startTime: EditText
    private lateinit var endTime: EditText
    private lateinit var eventLink: EditText
    private lateinit var scheduleButton:Button
    private lateinit var galleryButton:FloatingActionButton
    private var event: Event = Event("", "", "", "", "", "", "", "", "", "", "", "", "")
    private var mediaUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scheduling_event)

        // Set colors for status bar and navigation bar
        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)
        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)

        initializeViews()

        galleryButton.setOnClickListener {
            openGallery()
        }
        event.eventId=" "
        event.eventDescription = eventDescription.text.toString()
        event.eventName = eventName.text.toString()
        event.eventMode = eventMode.selectedItem.toString()
        event.platform = platform.selectedItem.toString()
        event.location = location.text.toString()
        event.startDate = startDate.text.toString()
        event.endDate = endDate.text.toString()
        event.startTime = startTime.text.toString()
        event.endTime = endTime.text.toString()
        event.eventLink = eventLink.text.toString()
        event.eventBanner = mediaUri.toString()

        scheduleButton.setOnClickListener {
            // Save event to database
        }

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }
    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaUri = result.data?.data
                eventBanner.setImageURI(mediaUri)
            }
        }

    fun initializeViews(){
        eventBanner = findViewById(R.id.previewImageView)
        eventName = findViewById(R.id.eventNameEditText)
        eventMode = findViewById(R.id.eventModeSpinner)
        platform = findViewById(R.id.eventPlatformSpinner)
        location = findViewById(R.id.locationEditText)
        eventDescription = findViewById(R.id.eventDetailsEditText)
        startDate = findViewById(R.id.startDate)
        endDate = findViewById(R.id.endDate)
        startTime = findViewById(R.id.startTime)
        endTime = findViewById(R.id.endTime)
        eventLink = findViewById(R.id.eventLinkEditText)
        scheduleButton = findViewById(R.id.scheduleButton)
        galleryButton = findViewById(R.id.galleryButton)

        populateSpinners()
    }

    private fun populateSpinners() {
        // Set adapter for Organization Type
        ArrayAdapter.createFromResource(
            this,
            R.array.event_mode_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            eventMode.adapter = adapter
        }

        // Set adapter for Organization Size
        ArrayAdapter.createFromResource(
            this,
            R.array.event_platform_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            platform.adapter = adapter
        }
    }
}