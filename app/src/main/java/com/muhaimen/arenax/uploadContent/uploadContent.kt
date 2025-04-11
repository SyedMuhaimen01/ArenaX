package com.muhaimen.arenax.uploadContent

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
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
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Track
import com.muhaimen.arenax.dataClasses.UserData

import com.muhaimen.arenax.utils.Constants

import com.muhaimen.arenax.userProfile.UserProfile
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*

class UploadContent : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var previewImageView: ImageView
    private lateinit var captionEditText: EditText
    private lateinit var galleryButton: ImageButton
    private lateinit var cameraButton: ImageButton
    private lateinit var uploadPostButton: FloatingActionButton
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
    private lateinit var musicButton: ImageButton
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
            if (startSeekBar.progress < endSeekBar.progress) {
                adapter.pauseAudio()
                playPauseButton.text = "Play"
                adapter.selectedTrack?.let { it1 -> trimAudio(it1) }
                trimTrackLayout.visibility = View.GONE
                previewImageView.visibility = View.VISIBLE
                captionEditText.visibility = View.VISIBLE
                uploadToolbar.visibility = View.VISIBLE
                isPlaying = false
            } else { }
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
            releaseMediaPlayer()
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
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
            val userId = auth.currentUser?.uid
            if (userId != null) {
                uploadToFirebaseStorage(userId, caption)
            } else { }
        } else {
            Toast.makeText(this, "Please select media to upload", Toast.LENGTH_SHORT)
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
            }.addOnFailureListener {}
        }
    }

    private fun savePostDetailsToServer(userId: String, mediaUrl: String, caption: String) {
        val requestQueue = Volley.newRequestQueue(this)

        // Fetch user location details from the backend
        fetchUserLocation(
            context = this,
            firebaseUid = userId,
            onSuccess = { city, country, latitude, longitude ->
                // Preparing the JSON payload with location data
                val jsonRequest = JSONObject().apply {
                    put("userId", userId)
                    put("content", mediaUrl)
                    put("caption", caption)
                    put("sponsored", false)
                    put("city", city)
                    put("country", country)
                    put("latitude", latitude ?: 0.0)
                    put("longitude", longitude ?: 0.0)
                    put("created_at", System.currentTimeMillis())
                    put("trimmed_audio_url", trimmedAudioUrl)
                }

                // Create the POST request
                val postRequest = object : JsonObjectRequest(
                    Request.Method.POST,
                    "${Constants.SERVER_URL}uploads/uploadPost",
                    jsonRequest,
                    { _ ->
                        // Success response
                        Toast.makeText(this@UploadContent, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent("NEW_POST_ADDED")
                        LocalBroadcastManager.getInstance(this@UploadContent).sendBroadcast(intent)
                    },
                    { error ->
                        // Error response
                        //Toast.makeText(this@UploadContent, "Failed to upload post: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    // Customize the retry policy to wait for 2 minutes and disable retries
                    override fun getRetryPolicy(): RetryPolicy {
                        return DefaultRetryPolicy(
                            120000, // Timeout duration in milliseconds (2 minutes)
                            0,      // No retries (set maxRetries to 0)
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                        )
                    }
                }

                // Add the request to the queue
                requestQueue.add(postRequest)
            },
            onError = { error ->
                // Handle location fetch error
                //Toast.makeText(this@UploadContent, "Failed to fetch location: $error", Toast.LENGTH_SHORT).show()
            }
        )

        // Release media player resources
        releaseMediaPlayer()
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 101
    }

    private fun fetchTracks() {
        val url = "${Constants.SERVER_URL}fetchSongs/data"

        // Create a StringRequest to fetch tracks
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
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
        requestQueue.add(stringRequest)
    }
    @SuppressLint("WrongConstant")
    fun trimAudio(track: Track) {
        val audioUrl = track.downloadUrl
        trimTrackLayout.visibility = View.VISIBLE
        searchLinearLayout.visibility = View.GONE

        val startTimeUs = startSeekBar.progress * 1_000_000L // Convert seconds to microseconds
        val endTimeUs = endSeekBar.progress * 1_000_000L // Convert seconds to microseconds

        if (endTimeUs <= startTimeUs) return

        val outputPath = "${externalCacheDir?.absolutePath}/trimmed_audio.aac" // Output file will be AAC
        trimmedAudioUrl = outputPath // Store trimmed audio URL

        Log.d("Output Path", "Output path for trimmed audio: $outputPath")

        val outputFile = File(outputPath)
        if (outputFile.exists()) {
            outputFile.delete()
            Log.d("Output File", "Existing output file deleted.")
        }

        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(audioUrl)

            // Find the audio track index
            val trackIndex = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: throw IllegalArgumentException("No audio track found")

            // Select the audio track
            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)

            // Set up the muxer (using AAC as output format)
            val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val audioTrackIndex = muxer.addTrack(format)
            muxer.start()

            val buffer = ByteBuffer.allocate(1024 * 1024) // Buffer size
            val bufferInfo = MediaCodec.BufferInfo()

            // Seek to the start time
            extractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            // Read and write audio samples to the output
            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)

                if (bufferInfo.size < 0 || extractor.sampleTime > endTimeUs) break

                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags

                // Write the sample data to the muxer
                muxer.writeSampleData(audioTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            // Stop and release resources
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
                mediaPlayer!!.isLooping = true
                mediaPlayer!!.start()
                Log.d("MediaPlayer", "Playing trimmed audio in loop.")
            }

            mediaPlayer!!.setOnCompletionListener {
                Log.d("MediaPlayer", "Trimmed audio playback completed. It will restart.")
            }
        } catch (e: IOException) {
            Log.e("MediaPlayer", "Error playing trimmed audio: ${e.message}")
        }
    }

    private fun setupAutoComplete() {
        val trackNames = TrackList.map { it.title }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, trackNames)
        searchBar.setAdapter(adapter)

        searchBar.setOnItemClickListener { _, _, position, _ ->
            val selectedTrackName = searchBar.adapter.getItem(position).toString()
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
        val filteredList = if (query.isEmpty()) {
            TrackList
        } else {
            TrackList.filter { it.title.contains(query, ignoreCase = true) }
        }
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

    override fun onStop(){
        super.onStop()
        releaseMediaPlayer()
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
        }
    }

    private fun fetchUserLocation(
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