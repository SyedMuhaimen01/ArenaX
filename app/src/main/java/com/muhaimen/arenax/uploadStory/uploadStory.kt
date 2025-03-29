package com.muhaimen.arenax.uploadStory


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.net.ConnectivityManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.DraggableText
import com.muhaimen.arenax.dataClasses.Track
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.uploadContent.UploadContent.Companion.CAMERA_PERMISSION_REQUEST_CODE
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.UUID


class uploadStory : AppCompatActivity() {

    private lateinit var storyPreviewImageView: ImageView
    private lateinit var textButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var cameraButton: ImageButton
    private lateinit var uploadButton: FloatingActionButton
    private lateinit var backButton: ImageButton
    private lateinit var userData: UserData
    private lateinit var auth: FirebaseAuth
    private val firebaseStorage = FirebaseStorage.getInstance()
    private lateinit var databaseReference: DatabaseReference
    private var selectedImageUri: Uri? = null
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var adapter: TracksAdapter
    private lateinit var musicButton: ImageButton
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
    val fixedDuration=15 //15 seconds
    private var mediaUri: Uri? = null
    private var trimmedAudioUrl:String?=null
    private lateinit var draggableContainers: MutableList<FrameLayout>
    private var draggableTextList = mutableListOf<DraggableText>()
    private var mediaPlayer: MediaPlayer? = null
    private var backgroundColor:Int?=null
    private var textColor:Int?=null
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_story)
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
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
        auth=FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("userData").child(auth.currentUser?.uid ?: "")
        backButton.setOnClickListener(){
            finish()
        }

        // Hide trimming options initially
        trimTrackLayout.visibility = View.GONE
        // Setup RecyclerView
        setupAutoComplete()
        setupSearchFilter()
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TracksAdapter(this,emptyList()) { track -> trimAudio(track) }
        tracksRecyclerView.adapter = adapter
        fetchTracks()

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
            } else { }
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
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        // Button for uploading the story
        uploadButton.setOnClickListener {
            uploadStoryToBackend()
            adapter.releasePlayer()
            releaseMediaPlayer()
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }

        // Button to add draggable text
        textButton.setOnClickListener {
            createDraggableText()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

        val dialogBuilder = AlertDialog. Builder(this, android.R.style.ThemeOverlay_Material_Dark_ActionBar)
            .setTitle("Are you sure?")
            .setMessage("Do you really want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                // Finish the activity and release resources
                adapter.releasePlayer()
                finish()
            }
            .setNegativeButton("No", null)

// Create and show the dialog
        val dialog = dialogBuilder.create()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaUri = result.data?.data
                storyPreviewImageView.setImageURI(mediaUri)
            }
        }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaUri = result.data?.data
                storyPreviewImageView.setImageURI(mediaUri)
            }
        }

    // Function to check camera permission
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        adapter.releasePlayer()
        releaseMediaPlayer()
    }

    private fun uploadStoryToBackend() {
        if (mediaUri != null) {
            val userId = auth.currentUser?.uid ?: ""

            // Fetch user details from Firebase
            userId.let { uid ->
                databaseReference.get().addOnSuccessListener { dataSnapshot ->
                    val username = dataSnapshot.child("fullname").getValue(String::class.java)?.toString() ?:""
                    val profilePicture = dataSnapshot.child("profilePicture").getValue(String::class.java)?.toString()?:""
                    fetchUserLocation(
                        context = this@uploadStory,
                        firebaseUid = userId,
                        onSuccess = { city, country, latitude, longitude ->

                            val draggableTexts = getDraggableTextContent()
                            val mediaUrl = mediaUri.toString()
                            val duration = fixedDuration
                            val uploadTime = System.currentTimeMillis()

                            val storyJson = JSONObject().apply {
                                put("id", userId)
                                put("duration", duration)
                                put("trimmed_audio_url", trimmedAudioUrl ?: JSONObject.NULL)
                                put("created_at", uploadTime)
                                put("full_name", username)
                                put("profile_picture_url", profilePicture)
                                put("city", city ?: JSONObject.NULL)
                                put("country", country ?: JSONObject.NULL)
                                put("latitude", latitude ?: JSONObject.NULL)
                                put("longitude", longitude ?: JSONObject.NULL)

                                if (draggableTexts.isNotEmpty()) {
                                    val draggableTextsArray = JSONArray()
                                    draggableTexts.forEach { draggableText ->
                                        val textObject = JSONObject().apply {
                                            put("content", draggableText.content)
                                            put("x", draggableText.x)
                                            put("y", draggableText.y)
                                            put("backgroundColor", draggableText.backgroundColor)
                                            put("textColor", draggableText.textColor)
                                        }
                                        draggableTextsArray.put(textObject)
                                    }
                                    put("draggable_texts", draggableTextsArray)
                                } else {
                                    put("draggable_texts", JSONArray())
                                }
                            }

                            Log.d("UploadStory", storyJson.toString())
                            uploadToFirebaseStorage(storyJson)
                        },
                        onError = { error ->
                            Toast.makeText(this@uploadStory, "Location error: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                releaseMediaPlayer()
            }
        } else {
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadToFirebaseStorage(storyJson: JSONObject) {
        val mediaRef = firebaseStorage.reference.child("stories/${UUID.randomUUID()}")

        mediaUri?.let { uri ->
            val uploadTask = mediaRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                mediaRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    storyJson.put("mediaUrl", downloadUri.toString())
                    saveStoryToServer(storyJson)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveStoryToServer(storyJson: JSONObject) {
        val requestQueue = Volley.newRequestQueue(this)
        val postRequest = JsonObjectRequest(
            Request.Method.POST,
            "${Constants.SERVER_URL}stories/storyUpload",
            storyJson,
            { response ->
                // Handle success response
                val intent = Intent("NEW_STORY_ADDED")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
            },
            { error -> }
        )
        requestQueue.add(postRequest)
    }

    private var isEditable = true
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
            setTextSize(20f)
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

            // Set the text to bold
            typeface = Typeface.create(typeface, Typeface.BOLD)
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
            draggableTextList.remove(DraggableText(
                draggableText.text.toString(),
                container.x,
                container.y,
                backgroundColor,
                textColor
            ))
        }

        // Set click listener to save the text and disable editing
        tickButton.setOnClickListener {
            draggableText.isEnabled = false // Disable editing when tick is clicked
            draggableText.clearFocus() // Remove focus from EditText

            // Create a new DraggableText object with the current state
            draggableTextList.add(DraggableText(
                draggableText.text.toString(),
                container.x,
                container.y,
                backgroundColor,
                textColor// Get the background color as ColorStateList
            ))
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
                        backgroundColor = Color.BLACK
                        textColor = Color.WHITE
                        backgroundState = 1 // Update state to black background with white text
                    }
                    1 -> { // Black background, change to transparent with black text
                        draggableText.setBackgroundColor(Color.TRANSPARENT)
                        draggableText.setTextColor(Color.BLACK) // Set text color to black
                        backgroundColor = Color.TRANSPARENT
                        textColor = Color.BLACK
                        backgroundState = 2 // Update state to transparent with black text
                    }
                    2 -> { // Transparent with black text, change to transparent with white text
                        draggableText.setBackgroundColor(Color.TRANSPARENT)
                        draggableText.setTextColor(Color.WHITE) // Change text color to white
                        backgroundColor = Color.TRANSPARENT
                        textColor = Color.WHITE
                        backgroundState = 3 // Update state to transparent with white text
                    }
                    3 -> { // Transparent with white text, change back to default background
                        draggableText.setBackgroundColor(Color.WHITE)
                        draggableText.setTextColor(Color.BLACK) // Reset text color to default
                        backgroundColor = Color.WHITE
                        textColor = Color.BLACK
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


    private fun getDraggableTextContent(): List<DraggableText> {
        val rootLayout: ViewGroup = findViewById(R.id.uploadStoryLayout)
        val textContents = mutableListOf<DraggableText>() // Create a list to hold individual texts with their positions

        // Loop through the children of the root layout to find EditTexts
        for (i in 0 until rootLayout.childCount) {
            val view = rootLayout.getChildAt(i)
            if (view is FrameLayout) {
                // Get the EditText within the FrameLayout
                val editText = view.getChildAt(1) as? EditText // Change index if necessary
                editText?.let {
                    // Get the position of the EditText
                    val location = IntArray(2)
                    it.getLocationOnScreen(location)
                    val x = location[0].toFloat() // X-coordinate
                    val y = location[1].toFloat() // Y-coordinate


                    // Add the text and its position to the list
                    textContents.add(DraggableText(it.text.toString().trim(), x, y,backgroundColor,textColor))
                }
            }
        }

        return textContents // Return the list of texts with positions
    }

    private fun fetchTracks() {
        val url = "${Constants.SERVER_URL}fetchSongs/data"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val jsonArray = jsonObject.getJSONArray("results")
                    val trackList = mutableListOf<Track>()

                    for (i in 0 until jsonArray.length()) {
                        val trackJson = jsonArray.getJSONObject(i)
                        val track = Track(
                            id = trackJson.getString("id"),
                            artist = trackJson.getString("artist_name"),
                            title = trackJson.getString("name"),
                            artistId = trackJson.getString("artist_id"),
                            albumName = trackJson.getString("album_name"),
                            albumId = trackJson.getString("album_id"),
                            duration = trackJson.getInt("duration"),
                            audioUrl = trackJson.getString("audio"),
                            albumImage = trackJson.getString("image"),
                            downloadUrl = trackJson.getString("audiodownload")
                        )
                        trackList.add(track)
                    }

                    // Update the adapter with the new track list
                    adapter.updateTracks(trackList)
                } catch (e: Exception) {
                    Log.e("uploadStory", "Error parsing tracks: ${e.message}")
                }
            },
            Response.ErrorListener { error -> }
        ) {
            override fun getRetryPolicy(): DefaultRetryPolicy {
                // Increase timeout to 10 seconds (default is 2500ms)
                return DefaultRetryPolicy(
                    10000, // Timeout in milliseconds
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }
        }

        requestQueue.add(stringRequest)
    }

    @SuppressLint("DefaultLocale", "WrongConstant")
    fun trimAudio(track: Track) {
        Log.d("TrimAudio", "trimAudio function called.")

        val audioUrl = track.downloadUrl
        trimTrackLayout.visibility = View.VISIBLE
        searchLinearLayout.visibility = View.GONE
        Log.d("Trim Layout", "Trimming layout made visible.")

        val startTimeUs = startSeekBar.progress * 1_000_000L // Convert to microseconds
        val endTimeUs = endSeekBar.progress * 1_000_000L // Convert to microseconds

        if (endTimeUs <= startTimeUs) {
            Log.e("TrimAudio", "End time must be greater than start time.")
            return
        }

        val outputPath = "${externalCacheDir?.absolutePath}/trimmed_audio.mp3"
        Log.d("Output Path", "Output path for trimmed audio: $outputPath")

        val outputFile = File(outputPath)
        if (outputFile.exists()) {
            outputFile.delete()
            Log.d("Output File", "Existing output file deleted.")
        }

        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(audioUrl)

            val trackIndex = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: throw IllegalArgumentException("No audio track found")

            extractor.selectTrack(trackIndex)

            val format = extractor.getTrackFormat(trackIndex)
            val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val audioTrackIndex = muxer.addTrack(format)

            muxer.start()

            val buffer = ByteBuffer.allocate(1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()

            extractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)

                if (bufferInfo.size < 0 || extractor.sampleTime > endTimeUs) {
                    break
                }

                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(audioTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            extractor.release()

            Log.d("TrimAudio", "Trimming completed successfully.")
            playTrimmedAudio(outputPath)

        } catch (e: Exception) {
            Log.e("TrimAudio", "Error trimming audio: ${e.message}")
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

    override fun onPause() {
        super.onPause()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause() // Pause the audio playback
            }
        }
        Log.d("MediaPlayer", "Audio playback paused.")
    }

    // Properly release the MediaPlayer when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer() // Release media player resources
    }

    // Release MediaPlayer resources to avoid memory leaks
    private fun releaseMediaPlayer() {
        adapter.releasePlayer()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop() // Stop the playback
            }
            it.release() // Release the resources
            mediaPlayer = null // Set to null after release
            Log.d("MediaPlayer", "MediaPlayer released.")
        }
    }

    fun fetchUserLocation(
        context: Context,
        firebaseUid: String,
        onSuccess: (city: String?, country: String?, latitude: Double?, longitude: Double?) -> Unit,
        onError: (error: String) -> Unit
    ) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)

        val url = "${Constants.SERVER_URL}api2/getUserLocation/$firebaseUid"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val city = response.optString("city")
                    val country = response.optString("country")
                    val latitude = response.optDouble("latitude", Double.NaN)
                    val longitude = response.optDouble("longitude", Double.NaN)
                    onSuccess(city, country, latitude, longitude)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onError("Failed to parse response: ${e.message}")
                }
            },
            { error ->
                error.printStackTrace()
                onError("Request failed: ${error.message}")
            }
        )
        requestQueue.add(jsonObjectRequest)
    }
}
