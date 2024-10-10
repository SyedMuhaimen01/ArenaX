package com.muhaimen.arenax.uploadStory

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import com.muhaimen.arenax.R
import java.io.IOException

class viewStory : AppCompatActivity() {

    // Declare views
    private lateinit var backButton: ImageButton
    private lateinit var storyImageView: ImageView
    private var mediaPlayer: MediaPlayer? = null // Change to nullable MediaPlayer
    private lateinit var draggableTextContainer: FrameLayout // Container for draggable text views

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_story)

        // Initialize views after setContentView
        backButton = findViewById(R.id.backButton)
        storyImageView = findViewById(R.id.ImageView)

        // Create a FrameLayout programmatically for draggable text views
        draggableTextContainer = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Add the FrameLayout to the main layout (assuming you have a parent layout in your XML)
        val mainLayout = findViewById<ViewGroup>(R.id.main) // Replace with your parent layout ID
        mainLayout.addView(draggableTextContainer)

        // Apply window insets for immersive UI
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the back button action to close the activity
        backButton.setOnClickListener {
            onBackPressed() // Call onBackPressed for better navigation handling
        }

        // Get media and audio URLs from intent
        val mediaUrl = intent.getStringExtra("MEDIA_URL")
        val audioUrl = intent.getStringExtra("Audio")
        Log.d("ViewStory", "Received audio URL: $audioUrl")
        val texts = intent.getStringExtra("Texts")

        // Load the media into the ImageView
        loadMedia(mediaUrl)

        // Use raw JSON to display draggable texts
        texts?.let { displayDraggableTexts(it) }

        // Check if audioUrl is not null before playing
        audioUrl?.let {
            playTrimmedAudio(it) // Call the function to play the trimmed audio
        }
    }

    private fun loadMedia(mediaUrl: String?) {
        mediaUrl?.let {
            val uri = Uri.parse(it)

            // Use Glide to load the image or video thumbnail into the ImageView
            Glide.with(this)
                .load(uri)
                .thumbnail(0.1f)
                .error(R.mipmap.appicon2) // Fallback image if loading fails
                .into(storyImageView)
        } ?: run {
            Log.e("ViewStory", "Media URL is null.")
        }
    }

    private fun displayDraggableTexts(json: String) {
        Log.d("DraggableTextsJson", "Received JSON string: $json") // Log the JSON string

        try {
            val jsonObject = JSONObject(json)
            val valuesArray = jsonObject.getJSONArray("values")

            // Log the received draggable texts
            Log.d("DraggableTexts", "Received draggable texts count: ${valuesArray.length()}")

            // Create and position TextView for each draggable text
            for (i in 0 until valuesArray.length()) {
                val valueObject = valuesArray.getJSONObject(i)
                val nameValuePairs = valueObject.getJSONObject("nameValuePairs")

                val content = nameValuePairs.getString("content")
                val x = nameValuePairs.getDouble("x").toFloat()
                val y = nameValuePairs.getDouble("y").toFloat()
                val backgroundColor = nameValuePairs.getString("backgroundColor")
                val textColor = nameValuePairs.getString("textColor")

                // Create the draggable text view with the specified content, position, and colors
                val textView = createDraggableTextView(content, x, y, backgroundColor, textColor)
                textView.textSize = 16f // Set text size
                draggableTextContainer.addView(textView)
            }
        } catch (e: JSONException) {
            Log.e("JSON", "JSON parsing error: ${e.message}")
        } catch (e: Exception) {
            Log.e("Error", "Unexpected error while parsing draggable texts: ${e.message}")
        }
    }

    private fun createDraggableTextView(content: String, x: Float, y: Float, backgroundColor: String, textColor: String): TextView {
        return TextView(this).apply {
            text = content // Set the text content
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            this.x = x
            this.y = y

            // Set the background color and text color
            try {
                setBackgroundColor(Color.parseColor(backgroundColor)) // Convert the background color from string
                setTextColor(Color.parseColor(textColor)) // Convert the text color from string
            } catch (e: IllegalArgumentException) {
                Log.e("ColorParseError", "Error parsing color: ${e.message}")
            }

            // Make sure the TextView is visible
            visibility = TextView.VISIBLE
        }
    }


    private fun playTrimmedAudio(outputPath: String) {
        try {
            // Initialize MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(outputPath)
                prepareAsync() // Prepare the player asynchronously

                setOnPreparedListener {
                    isLooping = true // Set looping
                    start() // Start playing when prepared
                    Log.d("MediaPlayer", "Playing trimmed audio in loop.")
                }

                setOnCompletionListener {
                    Log.d("MediaPlayer", "Trimmed audio playback completed. It will restart.")
                    // No need to release here, as it will loop indefinitely
                }
            }
        } catch (e: IOException) {
            Log.e("MediaPlayer", "Error playing trimmed audio: ${e.message}")
        }
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
    override fun onBackPressed() {
        releaseMediaPlayer() // Release media player resources
        super.onBackPressed() // Call super to finish the activity
    }

    // Release MediaPlayer resources to avoid memory leaks
    private fun releaseMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop() // Stop the playback
            }
            it.release() // Release the resources
            mediaPlayer = null // Set to null after release
            Log.d("MediaPlayer", "MediaPlayer released.")
        }
    }
}
