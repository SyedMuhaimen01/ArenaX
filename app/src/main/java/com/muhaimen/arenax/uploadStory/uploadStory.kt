package com.muhaimen.arenax.uploadStory

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.material.search.SearchBar
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.DraggableText
import com.muhaimen.arenax.dataClasses.Track
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.dataClasses.gamesData
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.time.Duration

class uploadStory : AppCompatActivity() {

    private lateinit var storyPreviewImageView: ImageView
    private lateinit var textButton: TextView
    private lateinit var galleryButton: TextView
    private lateinit var cameraButton: TextView
    private lateinit var uploadButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var userData: UserData
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2
    private var selectedImageUri: Uri? = null
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var adapter: TracksAdapter
    private lateinit var musicButton: TextView
     lateinit var searchLinearLayout: LinearLayout
    private lateinit var searchBar: AutoCompleteTextView
    private var TrackList: List<Track> = emptyList()
    // Variables for draggable text
    private var deltaX: Float = 0f
    private var deltaY: Float = 0f
    private lateinit var playPauseButton: Button
    private lateinit var trimButton: Button
    private lateinit var cancelButton: Button
     lateinit var startSeekBar: SeekBar
     lateinit var endSeekBar: SeekBar
    lateinit var trimTrackLayout: LinearLayout
    private var isPlaying = false
    private var startTime: Int = 0
    private var endTime: Int = 0
    val fixedDuration=15
    private var trimmedAudioUrl:String?=null
    private lateinit var draggableContainers: MutableList<FrameLayout>
    private var draggableTextList = mutableListOf<DraggableText>()
    private var mediaPlayer: MediaPlayer? = null
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_story)

        storyPreviewImageView = findViewById(R.id.previewImageView)
        textButton = findViewById(R.id.textButton)
        galleryButton = findViewById(R.id.galleryButton)
        cameraButton = findViewById(R.id.cameraButton)
        uploadButton = findViewById(R.id.uploadPostButton)
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView)
        musicButton = findViewById(R.id.musicButton)
        searchBar = findViewById(R.id.searchbar)
        playPauseButton = findViewById(R.id.playPauseButton)
        trimButton = findViewById(R.id.trimButton)
        cancelButton = findViewById(R.id.cancelButton)
        startSeekBar = findViewById(R.id.startSeekBar)
        endSeekBar = findViewById(R.id.endSeekBar)
        trimTrackLayout = findViewById(R.id.trimTrackLayout)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener(){
            finish()
        }
        // Initialize Firebase Auth
        auth = FirebaseManager.getAuthInstance()

        // Hide trimming options initially
        trimTrackLayout.visibility = View.GONE
        // Setup RecyclerView
        setupAutoComplete()
        setupSearchFilter()
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TracksAdapter(this,emptyList()) { track -> trimAudio(track) }
        tracksRecyclerView.adapter = adapter
        fetchTracks("83961508")
        searchLinearLayout = findViewById(R.id.searchLinearLayout)
        draggableContainers = mutableListOf()
        musicButton.setOnClickListener {
            // Change visibility of RecyclerView to VISIBLE when button is clicked
            if (searchLinearLayout.visibility == View.GONE) {
                searchLinearLayout.visibility = View.VISIBLE

                // Check if draggableContainers is initialized
                if (::draggableContainers.isInitialized) {
                    draggableContainers.forEach {
                        it.visibility = View.GONE
                    }
                } else {
                    Log.e("uploadStory", "draggableContainers is not initialized")
                }

            } else {
                searchLinearLayout.visibility = View.GONE

                // Check if draggableContainers is initialized
                if (::draggableContainers.isInitialized) {
                    draggableContainers.forEach {
                        it.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("uploadStory", "draggableContainers is not initialized")
                }
            }


        }

        // Set max to the duration in seconds
        Log.d("uploadStory", "Duration: ${startSeekBar.max}")

        startSeekBar.progress = 0
        endSeekBar.progress = fixedDuration



// Listener for the Start SeekBar
        startSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Calculate the new end position
                    val newEndPosition = progress + fixedDuration
                    if (newEndPosition <= adapter.selectedTrack?.duration!!) {
                        endSeekBar.progress = newEndPosition
                    } else {
                        // Ensure end SeekBar doesn't exceed max duration
                        endSeekBar.progress = adapter.selectedTrack?.duration!!
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

// Listener for the End SeekBar
        endSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Calculate the new start position
                    val newStartPosition = progress - fixedDuration
                    if (newStartPosition >= 0) {
                        startSeekBar.progress = newStartPosition
                    } else {
                        // Ensure start SeekBar doesn't go below zero
                        startSeekBar.progress = 0
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


        playPauseButton.setOnClickListener {
            if (isPlaying) {
                // Pause the audio and hide trimming options
                adapter.pauseAudio()
                playPauseButton.text = "Play"

            } else {
                // Play the audio and show trimming options
                adapter.selectedTrack?.let { it1 -> adapter.playTrack(it1) }
                playPauseButton.text = "Pause"

            }
            isPlaying = !isPlaying
        }

        trimButton.setOnClickListener {
            Log.d("uploadStory", "Start time: $startTime, End time: $endTime")
            if (startSeekBar.progress < endSeekBar.progress) {
                adapter.pauseAudio()
                playPauseButton.text = "Play"
                Log.d("uploadStory", "Trimming audio...")
                adapter.selectedTrack?.let { it1 -> trimAudio(it1) }
                trimTrackLayout.visibility = View.GONE
                isPlaying = false
            } else {
                Toast.makeText(this, "Invalid time range", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            adapter.stopAudio()
            playPauseButton.text = "Play"
            trimTrackLayout.visibility = View.GONE
            isPlaying = false
        }
        // Button for selecting an image from the gallery
        galleryButton.setOnClickListener {
            openGallery()
        }

        // Button for capturing an image using the camera
        cameraButton.setOnClickListener {
            captureImage()
        }

        // Button for uploading the story
        uploadButton.setOnClickListener {
            uploadStory()
        }

        // Button to add draggable text
        textButton.setOnClickListener {
            createDraggableText()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

        AlertDialog.Builder(this)
            .setTitle("Are you sure?")
            .setMessage("Do you really want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                // Finish the activity to end all processes related to it
                adapter.releasePlayer()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                storyPreviewImageView.setImageURI(selectedImageUri)
            }
        }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val bitmap = result.data?.extras?.get("data") as Bitmap
                storyPreviewImageView.setImageBitmap(bitmap)
            }
        }

    override fun onPause() {
        super.onPause()
        adapter.releasePlayer()
    }
    override fun onStop() {
        super.onStop()
        // Stop any background tasks or services if needed
        adapter.releasePlayer()// Example function to stop background tasks
    }
    private fun uploadStory() {
        if (selectedImageUri != null) {
            val userId = auth.currentUser?.uid
            userData = UserData(userId = userId.toString())

            // Get the list of draggable texts
            val draggableTexts = getDraggableTextContent() // This function should return a list of DraggableText objects
            val mediaUrl = selectedImageUri.toString() // Image URL
            val duration = 24 * 60 * 60 // Duration in seconds (24 hours)

            // Create JSON object for the story
            val storyJson = JSONObject().apply {
                put("user_id", userData.userId)
                put("media_url", mediaUrl)
                put("created_at", System.currentTimeMillis())
                put("duration", duration)
                put("trimmed_audio_url", trimmedAudioUrl)

                // Create a JSON array for draggable texts
                if (draggableTextList.isNotEmpty()) {
                    val draggableTextsArray = JSONArray()
                    // Loop through the existing list of draggable texts and convert each to a JSON object
                    draggableTextList.forEach { draggableText ->
                        val textObject = JSONObject().apply {
                            put("content", draggableText.content) // Content of the draggable text
                            put("x", draggableText.x)             // X-coordinate
                            put("y", draggableText.y)             // Y-coordinate
                        }
                        draggableTextsArray.put(textObject) // Add the JSONObject to the JSONArray
                    }
                    put("draggable_texts", draggableTextsArray) // Save the JSONArray into the main JSON object
                } else {
                    put("draggable_texts", JSONObject.NULL) // If no draggable texts, save a null value
                }

            }
            Log.d("UploadStory", storyJson.toString())

            // Send data to the backend
            saveStoryToServer(storyJson)
        } else {
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStoryToServer(storyJson: JSONObject) {
        val requestQueue = Volley.newRequestQueue(this)

        val postRequest = JsonObjectRequest(
            Request.Method.POST,
            "http://192.168.100.6:3000/stories/uploadStory", // Your backend endpoint
            storyJson,
            { response ->
                // Handle success response
                Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
            },
            { error ->
                // Handle error
                Toast.makeText(this, "Error uploading story: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        requestQueue.add(postRequest)
    }



    private var isEditable = true // Track the editable state of the EditText
     @SuppressLint("ClickableViewAccessibility")
     fun createDraggableText() {
        // Variable to track background state: 0 = default, 1 = black background with white text, 2 = transparent with black text, 3 = transparent with white text
        var backgroundState = 0

        // Create a container layout (FrameLayout) to hold the EditText and buttons
        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 20, 20, 20)
            }
        }
        container.x = 200f // Set your desired initial x position
        container.y = 200f // Set your desired initial y position
        draggableContainers.add(container)
        // Create the EditText
        val draggableText = EditText(this).apply {
            hint = "Type your text"
            setTextColor(Color.BLACK)
            setBackgroundResource(R.drawable.curved_rectangle)
            setPadding(20, 20, 20, 20) // Set padding for the text inside the EditText
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(50, 50, 50, 50) // Let the EditText wrap its content
            }
            isFocusableInTouchMode = true // Allow the EditText to receive input when clicked
            isEnabled = true // By default, make it editable
        }
        // Create the delete button
        val deleteButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_delete) // Use a delete icon
            background = null // Remove background
            setPadding(0, 0, 0, 0) // Optional padding adjustments
            layoutParams = FrameLayout.LayoutParams(
                60, // Width of the delete button
                60, // Height of the delete button
                Gravity.END or Gravity.TOP // Position the delete button at the top-right corner
            )
        }

        // Create the tick button
        val tickButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.checkbox_on_background) // Use a tick icon
            background = null // Remove background
            setPadding(0, 0, 0, 0) // Optional padding adjustments
            layoutParams = FrameLayout.LayoutParams(
                60, // Width of the tick button
                60, // Height of the tick button
                Gravity.START or Gravity.TOP // Position the tick button at the top-left corner
            )

        }

        // Set click listener to delete the container
        deleteButton.setOnClickListener {
            val parent = container.parent as ViewGroup
            parent.removeView(container) // Remove the entire container
            draggableTextList.remove(DraggableText(draggableText.text.toString(),draggableText.x,draggableText.y))
        }

        // Set click listener to save the text and disable editing
        tickButton.setOnClickListener {
            draggableText.isEnabled = false // Disable editing when tick is clicked
            draggableText.clearFocus() // Remove focus from EditText
            draggableTextList.add(DraggableText(draggableText.text.toString(),draggableText.x,draggableText.y))
        }

        // Create a GestureDetector for double-click detection (for transparent background)
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // Set the background to transparent on double-click
                draggableText.setBackgroundColor(Color.TRANSPARENT)
                backgroundState = 2 // Update the state to transparent with black text
                return true
            }
        })

        // Set touch listener for the EditText to handle double-clicks
        draggableText.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event) // Pass the event to the gesture detector
            false // Return false to allow other touch events
        }

        // Set click listener for the EditText to toggle background color and enable editing
        draggableText.setOnClickListener {
            // If the EditText is not enabled, enable it for editing
            if (!draggableText.isEnabled) {
                draggableText.isEnabled = true
                draggableText.requestFocus() // Focus back to EditText for editing
            } else {
                // Toggle background color based on the current state
                when (backgroundState) {
                    0 -> { // Default state, change to black background with white text
                        draggableText.setBackgroundColor(Color.BLACK)
                        draggableText.setTextColor(Color.WHITE) // Change text color for contrast
                        backgroundState = 1 // Update state to black background with white text
                    }
                    1 -> { // Black background, change to transparent with black text
                        draggableText.setBackgroundColor(Color.TRANSPARENT)
                        draggableText.setTextColor(Color.BLACK) // Set text color to black
                        backgroundState = 2 // Update state to transparent with black text
                    }
                    2 -> { // Transparent with black text, change to transparent with white text
                        draggableText.setBackgroundColor(Color.TRANSPARENT)
                        draggableText.setTextColor(Color.WHITE) // Change text color to white
                        backgroundState = 3 // Update state to transparent with white text
                    }
                    3 -> { // Transparent with white text, change back to default background
                        draggableText.setBackgroundResource(R.drawable.curved_rectangle)
                        draggableText.setTextColor(Color.BLACK) // Reset text color to default
                        backgroundState = 0 // Update state to default background
                    }
                }
            }
        }

        // Add the EditText and buttons to the container
        container.addView(tickButton)
        container.addView(draggableText)
        container.addView(deleteButton)

        // Add the container to the root layout
        val rootLayout: ViewGroup = findViewById(R.id.uploadStoryLayout)
        rootLayout.addView(container)

        // Set touch listener for dragging the FrameLayout (container)
        container.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    deltaX = event.rawX - v.x
                    deltaY = event.rawY - v.y
                }

                MotionEvent.ACTION_MOVE -> {
                    v.animate()
                        .x(event.rawX - deltaX)
                        .y(event.rawY - deltaY)
                        .setDuration(0)
                        .start()
                }
            }
            true
        }
    }


    fun onDoubleTap(e: MotionEvent?): Boolean {
        // Handle double-tap event
        return true
    }
    private fun getDraggableTextContent(): List<DraggableText> {
        val rootLayout: ViewGroup = findViewById(R.id.uploadStoryLayout)
        val textContents = mutableListOf<DraggableText>() // Create a list to hold individual texts with their positions

        // Loop through the children of the root layout to find EditTexts
        for (i in 0 until rootLayout.childCount) {
            val view = rootLayout.getChildAt(i)
            if (view is FrameLayout) {
                // Get the EditText within the FrameLayout
                val editText = view.getChildAt(0) as? EditText
                editText?.let {
                    // Get the position of the EditText
                    val location = IntArray(2)
                    it.getLocationOnScreen(location)
                    val x = location[0].toFloat() // X-coordinate
                    val y = location[1].toFloat() // Y-coordinate

                    // Add the text and its position to the list
                    textContents.add(DraggableText(it.text.toString().trim(), x, y))
                }
            }
        }

        return textContents // Return the list of texts with positions
    }

    private fun fetchTracks(userId: String) {
        // Example API endpoint to fetch tracks
        val url =
            "https://api.jamendo.com/v3.0/tracks/?client_id=${userId}&format=json&limit=200" // Replace with your API URL

        // Create a StringRequest to fetch tracks
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                // Log the raw response for debugging
                Log.d("uploadStory", "API Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    val jsonArray = jsonObject.getJSONArray("results") // Changed to "results"
                    val trackList = mutableListOf<Track>()

                    for (i in 0 until jsonArray.length()) {
                        val trackJson = jsonArray.getJSONObject(i)
                        val track = Track(
                            id = trackJson.getString("id"), // Adjusted to correct field
                            artist = trackJson.getString("artist_name"), // Adjusted to correct field
                            title = trackJson.getString("name"), // Adjusted to correct field
                            artistId = trackJson.getString("artist_id"), // Adjusted to correct field
                            albumName = trackJson.getString("album_name"), // Adjusted to correct field
                            albumId = trackJson.getString("album_id"), // Adjusted to correct field
                            duration = trackJson.getInt("duration"), // Adjusted to correct field
                            audioUrl = trackJson.getString("audio"), // Adjusted to correct field
                            albumImage = trackJson.getString("image"),
                            downloadUrl = trackJson.getString("audiodownload") // Added download URL
                        )
                        trackList.add(track)
                        TrackList=trackList
                    }

                    // Update the adapter with the new track list
                    adapter.updateTracks(trackList)
                } catch (e: Exception) {
                    Log.e("uploadStory", "Error parsing tracks: ${e.message}")
                }
            },
            { error ->
                Log.e("uploadStory", "Error fetching tracks: ${error.message}")
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest)
    }
    @SuppressLint("DefaultLocale")
    fun trimAudio(track: Track) {
        Log.d("TrimAudio", "trimAudio function called.")

        // Assuming `track` has an attribute `downloadUrl` that contains the audio URL
        val audioUrl = track.downloadUrl
        Log.d("Audio URL", "Audio URL: $audioUrl") // Log the audio URL

        // Set visibility for trimming layout
        trimTrackLayout.visibility = View.VISIBLE
        searchLinearLayout.visibility = View.GONE
        Log.d("Trim Layout", "Trimming layout made visible.")

        // Calculate the start and end times based on seek bar progress
        val startTime = startSeekBar.progress // in seconds
        val endTime = endSeekBar.progress // in seconds
        if (endTime <= startTime) {
            Toast.makeText(this, "End time must be greater than start time.", Toast.LENGTH_SHORT).show()
            return
        }
        val duration = endTime - startTime
        Log.d("Duration", "Calculated duration: $duration seconds")

        // Format start time to HH:MM:SS
        val formattedStartTime = String.format("%02d:%02d:%02d",
            startTime / 3600,
            (startTime % 3600) / 60,
            startTime % 60)

        Log.d("Formatted Start Time", "Start time formatted: $formattedStartTime")

        // Specify the output file path
        val outputPath = "${externalCacheDir?.absolutePath}/trimmed_audio.mp3"
        Log.d("Output Path", "Output path for trimmed audio: $outputPath")

        // Check if the output file already exists and delete it
        val outputFile = File(outputPath)
        if (outputFile.exists()) {
            outputFile.delete() // Delete existing file
            Log.d("Output File", "Existing output file deleted.")
        } else {
            Log.d("Output File", "No existing output file found.")
        }

        // Prepare the FFmpeg command
        val command = arrayOf(
            "-loglevel", "verbose",
            "-ss", formattedStartTime,
            "-i", audioUrl,
            "-t", duration.toString(),
            "-acodec", "libmp3lame", // Use libmp3lame for MP3 output
            outputPath
        )

        Log.d("FFmpeg Command", "FFmpeg command: ${command.joinToString(" ")}") // Log the full command

        // Execute the FFmpeg command asynchronously
        FFmpeg.executeAsync(command) { executionId, returnCode ->
            if (returnCode == 0) {
                Log.d("FFmpeg", "Trimming completed successfully.")
                // Play the trimmed audio
                playTrimmedAudio(outputPath)
            } else {
                Log.e("FFmpeg", "Error trimming audio: $returnCode")
            }
        }
    }

    fun playTrimmedAudio(outputPath: String) {
        try {
            trimmedAudioUrl=outputPath
            // Initialize MediaPlayer
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(outputPath)
            mediaPlayer!!.prepare() // Prepare the player asynchronously

            mediaPlayer!!.setOnPreparedListener {
                mediaPlayer!!.isLooping = true // Set looping
                mediaPlayer!!.start() // Start playing when prepared
                Log.d("MediaPlayer", "Playing trimmed audio in loop.")
            }

            mediaPlayer!!.setOnCompletionListener {
                Log.d("MediaPlayer", "Trimmed audio playback completed. It will restart.")
                // No need to release here, as it will loop indefinitely
            }
        } catch (e: IOException) {
            Log.e("MediaPlayer", "Error playing trimmed audio: ${e.message}")
        }
    }

    private fun setupAutoComplete() {

        val trackNames = TrackList.map { it.title }
        // Create an ArrayAdapter for AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, trackNames)
        searchBar.setAdapter(adapter)

        // Set up the item click listener for auto-complete suggestions
        searchBar.setOnItemClickListener { _, _, position, _ ->
            // When a suggestion is selected, filter the list based on the selected game
            val selectedTrackName = searchBar.adapter.getItem(position).toString()
            // Filter the list based on selected game
            filterTracksList(selectedTrackName)
        }
    }

    private fun setupSearchFilter() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the list based on the text entered in the search bar
                val searchQuery = s.toString()
                filterTracksList(searchQuery)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterTracksList(query: String) {

        // Filter games based on the query
        val filteredList = if (query.isEmpty()) {
            TrackList // Show all games if query is empty
        } else {
            TrackList.filter { it.title.contains(query, ignoreCase = true) } // Filter by game name
        }

        // Update the adapter with the filtered list
        adapter.updateTracks(filteredList)
    }


}
