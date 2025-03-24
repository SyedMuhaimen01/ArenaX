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
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Event
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
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
    private lateinit var gameName:EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var organizationName: String
    private  var organizationId: String? =null
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
            scheduleButton.isEnabled = false

            val event = getEventData()
            if (event != null) {
                if (mediaUri != null) {
                    uploadImageAndSendEvent(event)
                } else {
                    sendEventToBackend(event, null)
                }
            } else {
                // Re-enable the button if `getEventData()` fails
                scheduleButton.isEnabled = true
            }
        }

        setDateTimePickers()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaUri = result.data?.data
                mediaUri?.let {
                    eventBanner.setImageURI(it)
                }
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
        gameName=findViewById(R.id.gameNameEditText)

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
        if (eventName.text.toString().trim().isEmpty()) {
            eventName.error = "Event name is required"
            eventName.requestFocus()
            return null
        }
        if (gameName.text.toString().trim().isEmpty()) {
            gameName.error = "Game name is required"
            gameName.requestFocus()
            return null
        }
        if (location.text.toString().trim().isEmpty()) {
            location.error = "Location is required"
            location.requestFocus()
            return null
        }
        if (eventDescription.text.toString().trim().isEmpty()) {
            eventDescription.error = "Event description is required"
            eventDescription.requestFocus()
            return null
        }
        if (startDate.text.toString().trim().isEmpty()) {
            startDate.error = "Start date is required"
            startDate.requestFocus()
            return null
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Adjust the format as needed
        dateFormat.isLenient = false // Ensure strict parsing

        try {
            val enteredDate = dateFormat.parse(startDate.text.toString().trim())
            val currentDate = Calendar.getInstance().time // Get today's date

            if (enteredDate == null || enteredDate.before(currentDate)) {
                startDate.error = "Enter a valid start date (cannot be in the past)"
                startDate.requestFocus()
                return null
            }
        } catch (e: Exception) {
            // Handle invalid date format
            startDate.error = "Invalid date format"
            startDate.requestFocus()
            return null
        }

        if (endDate.text.toString().trim().isEmpty()) {
            endDate.error = "End date is required"
            endDate.requestFocus()
            return null
        }
        if (startTime.text.toString().trim().isEmpty()) {
            startTime.error = "Start time is required"
            startTime.requestFocus()
            return null
        }
        if (endTime.text.toString().trim().isEmpty()) {
            endTime.error = "End time is required"
            endTime.requestFocus()
            return null
        }

        if (eventLink.text.isEmpty()) {
            eventLink.error = "Website URL is required"
            eventLink.requestFocus()
            return null
        }
        if (!android.util.Patterns.WEB_URL.matcher(eventLink.text).matches()) {
            eventLink.error = "Please enter a valid website URL"
            eventLink.requestFocus()
            return null
        }
        if (mediaUri == null) {
            Toast.makeText(this, "Please select an event banner", Toast.LENGTH_SHORT).show()
            return null
        }
        return Event(
            eventId = "",
            organizationId = organizationName,
            eventName = eventName.text.toString().trim(),
            gameName = gameName.text.toString().trim(),
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
        val databaseRef = FirebaseDatabase.getInstance().getReference("organizationsData")
        val query = databaseRef.orderByChild("organizationName").equalTo(organizationName)

        query.get().addOnSuccessListener { snapshot ->
            for (data in snapshot.children) {
                val organization = data.getValue(OrganizationData::class.java)
                // Assuming email is a TextView and profileImage is an ImageView
                organizationId = organization?.organizationId
                val storageReference = FirebaseStorage.getInstance().reference.child("organizationContent/$organizationId/eventBanners/${UUID.randomUUID()}.jpg")
                mediaUri?.let { uri ->
                    val uploadTask = storageReference.putFile(uri)

                    uploadTask.addOnSuccessListener {
                        storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                            sendEventToBackend(event, downloadUri.toString())
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("FirebaseError", "Error uploading image", exception)
                    }
                }
            }


        }.addOnFailureListener { exception ->
            Log.e("FirebaseError", "Error fetching organization data", exception)
        }

        val intent = Intent(this, OrganizationHomePageActivity::class.java)
        intent.putExtra("organization_name", organizationName)
        startActivity(intent)

    }


    private var isSendingEvent = false  // Flag to prevent duplicate requests
    private lateinit var requestQueue: RequestQueue  // Use a single request queue
    private val REQUEST_TAG = "sendEventRequest"  // Unique tag for request cancellation

    private fun sendEventToBackend(event: Event, imageUrl: String?) {
        if (!::requestQueue.isInitialized) {
            requestQueue = Volley.newRequestQueue(this)  // Initialize only once
        }

        if (isSendingEvent) {
            Log.d("sendEventToBackend", "Request already in progress, skipping duplicate request.")
            return
        }

        isSendingEvent = true  // Set flag to indicate request in progress

        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("organization_name", event.organizationId)
            put("eventName", event.eventName)
            put("gameName", event.gameName)
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

        Log.d("Request", requestBody.toString())

        // Cancel any previous request with the same tag
        requestQueue.cancelAll(REQUEST_TAG)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, "${Constants.SERVER_URL}manageEvents/addEvent", requestBody,
            { response ->
                Log.d("sendEventToBackend", "Event successfully sent: $response")
                isSendingEvent = false  // Reset flag after success
                scheduleButton.isEnabled = true
            },
            { error ->
                isSendingEvent = false  // Reset flag on failure
                scheduleButton.isEnabled = true
            }
        ).apply {
            tag = REQUEST_TAG
        }

        requestQueue.add(jsonObjectRequest)  // Add request to queue
    }



}
