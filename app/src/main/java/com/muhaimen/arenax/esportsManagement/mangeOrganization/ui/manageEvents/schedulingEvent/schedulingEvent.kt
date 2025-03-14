package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents.schedulingEvent

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Event
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var scheduleButton: Button
    private lateinit var galleryButton: FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private lateinit var organizationName: String
    private lateinit var userId: String
    private var mediaUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scheduling_event)

        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)
        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)

        organizationName = intent.getStringExtra("organization_name") ?: ""

        initializeViews()

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        galleryButton.setOnClickListener { openGallery() }
        scheduleButton.setOnClickListener {
            val event = getEventData()
            if (event != null) {
                if (mediaUri != null) {
                    uploadImageAndSendEvent(event)
                } else {
                    sendEventToBackend(event, null)
                }
            }
        }

        setDateTimePickers()
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

    private fun initializeViews() {
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
        ArrayAdapter.createFromResource(
            this,
            R.array.event_mode_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            eventMode.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.event_platform_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            platform.adapter = adapter
        }
    }

    private fun setDateTimePickers() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val calendar = Calendar.getInstance()

        val datePicker = { editText: EditText ->
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                editText.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val timePicker = { editText: EditText ->
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                editText.setText(timeFormat.format(calendar.time))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        startDate.setOnClickListener { datePicker(startDate) }
        endDate.setOnClickListener { datePicker(endDate) }
        startTime.setOnClickListener { timePicker(startTime) }
        endTime.setOnClickListener { timePicker(endTime) }
    }

    private fun getEventData(): Event? {
        return Event(
            eventId = "",
            organizationId = organizationName,
            eventName = eventName.text.toString().trim(),
            eventMode = eventMode.selectedItem?.toString() ?: "Default Mode",
            platform = platform.selectedItem?.toString() ?: "Default Platform",
            location = location.text?.toString()?.takeIf { it.isNotEmpty() },
            eventDescription = eventDescription.text?.toString()?.takeIf { it.isNotEmpty() },
            startDate = startDate.text?.toString()?.takeIf { it.isNotEmpty() },
            endDate = endDate.text?.toString()?.takeIf { it.isNotEmpty() },
            startTime = startTime.text?.toString()?.takeIf { it.isNotEmpty() },
            endTime = endTime.text?.toString()?.takeIf { it.isNotEmpty() },
            eventLink = eventLink.text?.toString()?.takeIf { it.isNotEmpty() },
            eventBanner = mediaUri?.toString()
        )
    }

    private fun uploadImageAndSendEvent(event: Event) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("event_banners/${UUID.randomUUID()}.jpg")

        mediaUri?.let { uri ->
            fileRef.putFile(uri)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        sendEventToBackend(event, downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Log.e("FirebaseStorage", "Failed to upload image: ${it.message}")
                    sendEventToBackend(event, null)
                }
        }
    }

    private fun sendEventToBackend(event: Event, imageUrl: String?) {
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("organization_name", event.organizationId)
            put("eventName", event.eventName)
            put("eventMode", event.eventMode)
            put("platform", event.platform)
            put("location", event.location ?: JSONObject.NULL)
            put("eventDescription", event.eventDescription ?: JSONObject.NULL)
            put("startDate", event.startDate)
            put("endDate", event.endDate)
            put("startTime", event.startTime)
            put("endTime", event.endTime)
            put("eventLink", event.eventLink ?: JSONObject.NULL)
            put("eventBanner", imageUrl ?: JSONObject.NULL)
        }

        Volley.newRequestQueue(this).add(
            JsonObjectRequest(Request.Method.POST, "${Constants.SERVER_URL}manageEvents/addEvent", requestBody, {}, {})
        )
    }
}
