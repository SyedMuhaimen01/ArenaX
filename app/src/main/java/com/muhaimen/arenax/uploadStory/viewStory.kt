package com.muhaimen.arenax.uploadStory

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.DraggableText
import com.muhaimen.arenax.dataClasses.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class viewStory : AppCompatActivity() {
    private lateinit var storyImageView: ImageView
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var draggableTextContainer: FrameLayout
    private val handler = Handler()
    private lateinit var textureView: TextureView
    private val client = OkHttpClient()
    private lateinit var progressBar: ProgressBar
    private lateinit var timeAgoTextView: TextView
    private lateinit var profilePicture:ImageView

    private var currentIndex = 0
    private lateinit var storiesList: List<Story>
    private lateinit var textJson: String
    private lateinit var story: Story
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_story)

        storyImageView = findViewById(R.id.ImageView)
        textureView = findViewById(R.id.videoPlayerView)
        draggableTextContainer = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val mainLayout = findViewById<ViewGroup>(R.id.main)
        mainLayout.addView(draggableTextContainer)

        // Handling window insets
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        progressBar = findViewById(R.id.progressBar)
        timeAgoTextView = findViewById(R.id.timeAgoTextView)
        profilePicture = findViewById(R.id.ProfilePicture)

        // Fetch data from intent extras
        story = intent.getParcelableExtra("Story")!!
        Log.d("Story", story.toString())

        val gson = Gson()
        val draggableJson = gson.toJson(story.draggableTexts)
        textJson = draggableJson

        // Check if individual story data is received
        if (!story.mediaUrl.isNullOrEmpty()) {
            // Display single story data
            Log.d("ViewStory", "Displaying single story data.")
            displaySingleStory()
        } else {
            // Handle list of stories
            storiesList = intent.getParcelableArrayListExtra("storiesList") ?: emptyList()
            currentIndex = intent.getIntExtra("currentIndex", 0)

            if (storiesList.isNotEmpty()) {
                displayStory()
            }
        }

        // Handle next/previous story navigation
        textureView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (event.x > resources.displayMetrics.widthPixels / 2) {
                        nextStory()
                    } else {
                        previousStory()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun displaySingleStory() {
        // Convert uploadedAt to Date object if it's a String timestamp (optional, based on how the data is passed)
        val uploadedAtDate = story.uploadedAt?.let {
            try {
                Date(story.uploadedAt.toString())
            } catch (e: Exception) {
                null // Return null if parsing fails
            }
        }

        // Create a single Story object with the passed-in data
        val singleStory = Story(
            id = 0, // Unique ID if needed
            mediaUrl = story.mediaUrl ?: "",
            duration = story.duration,
            trimmedAudioUrl = story.trimmedAudioUrl,
            draggableTexts = textJson.let {
                try {
                    // Try converting the string to JSONArray, return null if it fails
                    JSONArray(it)
                } catch (e: Exception) {
                    null // Return null if the conversion fails
                }
            },
            uploadedAt = uploadedAtDate, // Use the uploadedAtDate to pass as Date object
            userName = story.userName , // Default to an empty string if userName is null
            userProfilePicture = story.userProfilePicture  // Default to an empty string if userProfilePicture is null
        )

        // Set the "time ago" text using the `timeAgo` property from Story
        timeAgoTextView.text = singleStory.timeAgo

        // Load media if mediaUrl is not null or empty
        if (singleStory.mediaUrl.isNotEmpty()) {
            loadMedia(singleStory.mediaUrl)
        }

        if(singleStory.userProfilePicture.isNotEmpty() && singleStory.userProfilePicture!="null")
        {
            Log.d("ProfilePicture", "Loading profile picture from URL: ${singleStory.userProfilePicture}")

            val uri = Uri.parse(singleStory.userProfilePicture)
            Glide.with(this)
                .load(uri)
                .thumbnail(0.1f)
                .error("")
                .into(profilePicture)
        }
        // Play audio if available
        singleStory.trimmedAudioUrl?.let {
            playTrimmedAudio(it)
        }

        // Display draggable texts if available
        singleStory.draggableTexts?.let {
            displayDraggableTexts(it.toString()) // Assuming it needs to be a String
        }

        // Set progress bar (to simulate progress over the story's duration)
        progressBar.max = 100  // Assuming the max value is 100% for the progress
        progressBar.progress = 0  // Start at 0%

        // Update progress bar based on duration
        val progressInterval = 1000L // 1 second interval for progress update
        val totalDuration = singleStory.duration.toLong()

        val progressRunnable = object : Runnable {
            var progress = 0

            override fun run() {
                if (progress < 100) {
                    progress += (100 * progressInterval / totalDuration).toInt()
                    progressBar.progress = progress
                    handler.postDelayed(this, progressInterval)
                } else {
                    progressBar.progress = 100
                }
            }
        }

        // Start updating progress
        handler.post(progressRunnable)

        // Navigate to the next story after the duration
        handler.postDelayed({ finish() }, totalDuration)
    }







    private fun displayStory() {
        Log.d("ViewStory", "Displaying story at index: $currentIndex")
        val currentStory = storiesList[currentIndex]

        // Set the "time ago" text
        timeAgoTextView.text = calculateTimeAgo(currentStory.uploadedAt)

        // Load media
        loadMedia(currentStory.mediaUrl)

        // Display draggable texts
        displayDraggableTexts(currentStory.draggableTexts.toString())

        // Load audio if available
        currentStory.trimmedAudioUrl?.let { audioUrl ->
            playTrimmedAudio(audioUrl)
        }

        // Set progress bar
        progressBar.max = storiesList.size
        progressBar.progress = currentIndex + 1 // Progress is 1-based

        // Navigate to the next story after duration
        handler.postDelayed({ nextStory() }, currentStory.duration.toLong())
    }


    private fun calculateTimeAgo(uploadedAt: Date?): String {
        if (uploadedAt == null) return "Unknown time"

        val now = System.currentTimeMillis()
        val diffInMillis = now - uploadedAt.time
        val secondsAgo = diffInMillis / 1000
        val minutesAgo = secondsAgo / 60
        val hoursAgo = minutesAgo / 60
        val daysAgo = hoursAgo / 24
        val weeksAgo = daysAgo / 7

        return when {
            secondsAgo < 60 -> "Just now"
            minutesAgo < 60 -> "$minutesAgo minutes ago"
            hoursAgo < 24 -> "$hoursAgo hours ago"
            daysAgo < 7 -> "$daysAgo days ago"
            weeksAgo < 52 -> "$weeksAgo weeks ago"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(uploadedAt)
        }
    }


    private fun loadMedia(mediaUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaType = getMediaType(mediaUrl)

                withContext(Dispatchers.Main) {
                    when {
                        mediaType != null && mediaType.startsWith("image/") -> {
                            storyImageView.visibility = View.VISIBLE
                            textureView.visibility = View.GONE
                            setImage(mediaUrl)
                        }
                        mediaType != null && mediaType.startsWith("video/mp4") -> {
                            storyImageView.visibility = View.GONE
                            textureView.visibility = View.VISIBLE
                            playVideo(mediaUrl)
                        }
                        else -> {
                            Log.e("ViewPost", "Unsupported media type: $mediaType")
                            // Optionally show an error message to the user
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewPost", "Error loading media: ${e.message}")
            }
        }
    }


    private fun nextStory() {
        if (currentIndex < storiesList.size - 1) {
            currentIndex++
            displayStory()
        } else {
            finish()
        }
    }


    private fun previousStory() {
        if (currentIndex > 0) {
            currentIndex--
            displayStory()
        }
    }

    private fun playVideo(videoPath: String) {
        val uri = Uri.parse(videoPath)
        Log.d("ViewPost", "Attempting to play video from path: $videoPath")

        // Set up the TextureView surface
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(videoPath)
                    setSurface(Surface(surface))
                    prepareAsync() // Prepare in the background

                    setOnPreparedListener {
                        isLooping = true // Loop the video
                        start() // Start playing the video
                        Log.d("ViewPost", "Video playback started.")
                    }

                    setOnInfoListener { mp, what, extra ->
                        when (what) {
                            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                                Log.d("ViewPost", "Buffering started")
                                // Show buffering indicator (if any)
                            }
                            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                                Log.d("ViewPost", "Buffering ended")
                                // Hide buffering indicator (if any)
                            }
                        }
                        true
                    }

                    setOnCompletionListener {
                        Log.d("ViewPost", "Video playback completed.")
                        seekTo(0) // Reset the video to the start
                        start() // Optionally, restart the video
                    }

                    setOnErrorListener { mp, what, extra ->
                        Log.e("ViewPost", "Error occurred while playing video. What: $what, Extra: $extra")
                        true // Returning true indicates that we've handled the error
                    }
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                mediaPlayer?.release()
                mediaPlayer = null
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    private suspend fun getMediaType(mediaUrl: String): String? {
        val request = Request.Builder()
            .url(mediaUrl)
            .head() // Use HEAD to get the headers only
            .build()

        return try {
            val response: Response = client.newCall(request).execute()
            response.header("Content-Type").also {
                Log.d("ViewPost", "Media type retrieved: $it")
            }
        } catch (e: Exception) {
            Log.e("ViewPost", "Error retrieving media type: ${e.message}")
            null
        }
    }

    private fun setImage(imageUrl: String) {

        if (imageUrl.isEmpty() || imageUrl == "null") {
            Log.e("ViewPost", "Image URL is empty.")
            return
        }
        try {
            val uri = Uri.parse(imageUrl)
            Log.d("setImage", "Attempting to load image from URL: $uri")
            Glide.with(this)
                .load(uri)
                .thumbnail(0.1f) // Show a thumbnail while loading
                .error("") // Show a default error image if loading fails
                .into(storyImageView)
            Log.d("ViewPost", "Attempting to load image from URL: $imageUrl")

        } catch (e: Exception) {
            Log.e("ViewPost", "Exception while loading image: ${e.message}")
        }
    }

    private fun displayDraggableTexts(json: String) {
        Log.d("DraggableTextsJson", "Received JSON string: $json")

        try {
            // Check if the JSON string is empty or represents an empty array
            if (json == "[]") {
                Log.d("DraggableTexts", "Received an empty array.")
                return  // No draggable texts to process
            }

            // Attempt to parse the string as a JSONArray if it's not empty
            val jsonArray = JSONArray(json)

            Log.d("DraggableTexts", "Received draggable texts count: ${jsonArray.length()}")

            for (i in 0 until jsonArray.length()) {
                val valueObject = jsonArray.getJSONObject(i)
                val nameValuePairs = valueObject.getJSONObject("nameValuePairs")

                val content = nameValuePairs.getString("content")
                val x = nameValuePairs.getDouble("x").toFloat()
                val y = nameValuePairs.getDouble("y").toFloat()
                val backgroundColor = nameValuePairs.getInt("backgroundColor")
                val textColor = nameValuePairs.getInt("textColor")

                Log.d("DraggableText", "Content: $content, X: $x, Y: $y, BG: $backgroundColor, Text: $textColor")
                val draggableText = DraggableText(content, x, y, backgroundColor, textColor)

                val textView = createDraggableTextView(draggableText)
                draggableTextContainer.addView(textView)
            }
        } catch (e: JSONException) {
            Log.e("JSON", "JSON parsing error draggable text: ${e.message}")
        } catch (e: Exception) {
            Log.e("Error", "Unexpected error while parsing draggable texts: ${e.message}")
        }
    }


    private fun createDraggableTextView(draggableText: DraggableText): TextView {
        Log.d("DraggableText", "Creating draggable text view: $draggableText")
        return TextView(this).apply {
            text = draggableText.content
            textSize = 20f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            this.x = draggableText.x
            this.y = draggableText.y
            setBackgroundColor(draggableText.getBackgroundColor())
            setTextColor(draggableText.getTextColor())
            visibility = View.VISIBLE
            typeface = Typeface.create(typeface, Typeface.BOLD)
        }
    }

    private fun playTrimmedAudio(outputPath: String) {
        if(outputPath!="null") {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(outputPath)
                    prepareAsync()

                    setOnPreparedListener {
                        isLooping = true
                        start()
                        Log.d("MediaPlayer", "Playing trimmed audio in loop.")
                    }

                    setOnCompletionListener {
                        Log.d("MediaPlayer", "Trimmed audio playback completed. It will restart.")
                    }
                }
            } catch (e: IOException) {
                Log.e("MediaPlayer", "Error playing trimmed audio: ${e.message}")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        Log.d("MediaPlayer", "Audio playback paused.")
    }

    override fun onDestroy() {
        Log.d("ActivityLifecycle", "onDestroy")
        super.onDestroy()

        releaseMediaPlayer()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        releaseMediaPlayer()
        super.onBackPressed()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
            Log.d("MediaPlayer", "MediaPlayer released.")
        }
    }
}
