package com.muhaimen.arenax.uploadContent

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Track
import com.muhaimen.arenax.dataClasses.UserData

import com.muhaimen.arenax.utils.Constants

import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class UploadContent : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var previewImageView: ImageView
    private lateinit var captionEditText: EditText
    private lateinit var galleryButton: TextView
    private lateinit var cameraButton: TextView
    private lateinit var uploadPostButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var userData: UserData
    private var mediaUri: Uri? = null
    private val firebaseStorage = FirebaseStorage.getInstance()
    private lateinit var playPauseButton: Button
    private lateinit var trimButton: Button
    private lateinit var cancelButton: Button
    lateinit var startSeekBar: SeekBar
    lateinit var endSeekBar: SeekBar
    lateinit var trimTrackLayout: LinearLayout
    private lateinit var uploadToolbar: LinearLayout
    private var isPlaying = false
    private var startTime: Int = 0
    private var endTime: Int = 0
    val fixedDuration=15
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var adapter: TracksAdapter
    private lateinit var musicButton: TextView
    lateinit var searchLinearLayout: LinearLayout
    private lateinit var searchBar: AutoCompleteTextView
    private var TrackList: List<Track> = emptyList()
    private var mediaPlayer: MediaPlayer? = null
    private var trimmedAudioUrl:String?=null
    private val sharedPreferences6 by lazy { getSharedPreferences("UserLocationPrefs", Context.MODE_PRIVATE) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_content)
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        previewImageView = findViewById(R.id.previewImageView)
        captionEditText = findViewById(R.id.captionEditText)
        galleryButton = findViewById(R.id.galleryButton)
        cameraButton = findViewById(R.id.cameraButton)
        uploadPostButton = findViewById(R.id.uploadPostButton)
        backButton = findViewById(R.id.backButton)
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView)
        musicButton = findViewById(R.id.musicButton)
        searchBar = findViewById(R.id.searchbar)
        playPauseButton = findViewById(R.id.playPauseButton)
        trimButton = findViewById(R.id.trimButton)
        cancelButton = findViewById(R.id.cancelButton)
        startSeekBar = findViewById(R.id.startSeekBar)
        endSeekBar = findViewById(R.id.endSeekBar)
        trimTrackLayout = findViewById(R.id.trimTrackLayout)
        uploadToolbar = findViewById(R.id.uploadToolbar)
        auth = FirebaseAuth.getInstance()
        trimTrackLayout.visibility = View.GONE
        // Setup RecyclerView
        setupAutoComplete()
        setupSearchFilter()
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TracksAdapter(this,emptyList()) { track -> trimAudio(track) }
        tracksRecyclerView.adapter = adapter
        fetchTracks()
        searchLinearLayout = findViewById(R.id.searchLinearLayout)

        musicButton.setOnClickListener {
            // Change visibility of RecyclerView to VISIBLE when button is clicked
            if (searchLinearLayout.visibility == View.GONE) {
                searchLinearLayout.visibility = View.VISIBLE
                previewImageView.visibility = View.GONE
                captionEditText.visibility = View.GONE
                uploadToolbar.visibility = View.GONE
                tracksRecyclerView.visibility=View.VISIBLE
            } else {
                searchLinearLayout.visibility = View.GONE
                previewImageView.visibility = View.VISIBLE
                captionEditText.visibility = View.VISIBLE
                uploadToolbar.visibility = View.VISIBLE
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
                previewImageView.visibility = View.VISIBLE
                captionEditText.visibility = View.VISIBLE
                uploadToolbar.visibility = View.VISIBLE
                isPlaying = false
            } else {
              //  Toast.makeText(this, "Invalid time range", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            adapter.stopAudio()
            playPauseButton.text = "Play"
            trimTrackLayout.visibility = View.GONE
            isPlaying = false
        }
        backButton.setOnClickListener {
            adapter.releasePlayer()
            finish()
        }
        // Gallery button action
        galleryButton.setOnClickListener {
            openGallery()
        }

        // Camera button action
        cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        // Upload post button action
        uploadPostButton.setOnClickListener {
            uploadContent()
            adapter.releasePlayer()
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
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

    // Function to open gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaUri = result.data?.data
                previewImageView.setImageURI(mediaUri)
            }
        }

    // Function to open camera
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaUri = result.data?.data
                previewImageView.setImageURI(mediaUri)
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

    // Function to upload content
    private fun uploadContent() {
        var caption = captionEditText.text.toString()
        if (mediaUri != null ) {
            if (caption.isBlank()) {
                caption=""
            }
            val userId = auth.currentUser?.uid // Get the current user's ID
            if (userId != null) {
                uploadToFirebaseStorage(userId, caption)
            } else {
             //   Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select media and write a caption.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Function to upload media to Firebase Storage
    private fun uploadToFirebaseStorage(userId: String, caption: String) {
        val mediaRef = firebaseStorage.reference.child("uploads/${UUID.randomUUID()}")

        mediaUri?.let { uri ->
            val uploadTask = mediaRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                mediaRef.downloadUrl.addOnSuccessListener { downloadUri ->

                    savePostDetailsToServer(userId, downloadUri.toString(), caption)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePostDetailsToServer(userId: String, mediaUrl: String, caption: String) {
        userData = UserData(userId = userId)

        // Load location details (city, country, latitude, longitude)
        val (city, country, coordinates) = loadLocationFromSharedPreferences()
        val latitude = coordinates?.first ?: 0.0 // Default to 0.0 if coordinates are null
        val longitude = coordinates?.second ?: 0.0 // Default to 0.0 if coordinates are null

        val requestQueue = Volley.newRequestQueue(this)

        val jsonRequest = JSONObject().apply {
            put("userId", userData.userId)
            put("content", mediaUrl)
            put("caption", caption)
            put("sponsored", false)
            put("city", city) // City from shared preferences
            put("country", country) // Country from shared preferences
            put("latitude", latitude) // Latitude from shared preferences
            put("longitude", longitude) // Longitude from shared preferences
            put("created_at", System.currentTimeMillis())
            put("trimmed_audio_url", trimmedAudioUrl)
        }

        val postRequest = JsonObjectRequest(
            Request.Method.POST,
            "${Constants.SERVER_URL}uploads/uploadPost",
            jsonRequest,
            { _ ->
                Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent("NEW_POST_ADDED")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            },
            { error ->
                // Handle error if needed
            }
        )

        requestQueue.add(postRequest)
    }


    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 101
    }

    private fun fetchTracks() {
        // Example API endpoint to fetch tracks
        val url =
            "${Constants.SERVER_URL}fetchSongs/data"

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
    fun trimAudio(track: Track) {
        Log.d("TrimAudio", "trimAudio function called.")

        // Assuming `track` has an attribute `downloadUrl` that contains the audio URL
        val audioUrl = track.downloadUrl
        Log.d("Audio URL", "Audio URL: $audioUrl") // Log the audio URL

        // Set visibility for trimming layout
        trimTrackLayout.visibility = View.VISIBLE
        searchLinearLayout.visibility = View.GONE
        Log.d("Trim Layout", "Trimming layout made visible.")

        // Calculate the start and end times based on seek bar progress (in milliseconds)
        val startTime = startSeekBar.progress * 1000 // Convert seconds to milliseconds
        val endTime = endSeekBar.progress * 1000 // Convert seconds to milliseconds
        if (endTime <= startTime) {
         //   Toast.makeText(this, "End time must be greater than start time.", Toast.LENGTH_SHORT).show()
            return
        }
        val duration = endTime - startTime
        Log.d("Duration", "Calculated duration: $duration milliseconds")

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
            "-ss", (startTime / 1000).toString(), // FFmpeg expects seconds, so divide by 1000
            "-i", audioUrl,
            "-t", (duration / 1000).toString(), // FFmpeg expects seconds, so divide by 1000
            "-acodec", "libmp3lame", // Use libmp3lame for MP3 output
            outputPath
        )
        trimmedAudioUrl=outputPath

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

    // Handle back button press and release the MediaPlayer

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

    private fun loadLocationFromSharedPreferences(): Triple<String?, String?, Pair<Double, Double>?> {
        val city = sharedPreferences6.getString("city", null)
        val country = sharedPreferences6.getString("country", null)

        // Retrieve latitude and longitude as Floats, and then convert them back to Doubles
        val latitude = sharedPreferences6.getFloat("latitude", 0f).toDouble()
        val longitude = sharedPreferences6.getFloat("longitude", 0f).toDouble()

        // Return the data in a Triple: City, Country, and the Pair of latitude and longitude
        return Triple(city, country, Pair(latitude, longitude))
    }
}
