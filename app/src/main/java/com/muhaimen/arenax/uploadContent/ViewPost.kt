package com.muhaimen.arenax.uploadContent

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ViewPost : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var imageView: ImageView
    private lateinit var postCaption: TextView // TextView for caption
    private lateinit var seeMoreButton: Button // Button to toggle full caption
    private lateinit var likeCount: TextView // TextView for likes
    private lateinit var commentCount: TextView // TextView for comments
    private lateinit var shareCount: TextView // TextView for shares
    private lateinit var playerView: PlayerView // ExoPlayer view for video
    private var exoPlayer: ExoPlayer? = null

    private val client = OkHttpClient() // Initialize OkHttpClient
    private var isExpanded: Boolean = false // Flag to track caption expansion

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_post)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        // Initialize views
        backButton = findViewById(R.id.backButton)
        imageView = findViewById(R.id.ImageView) // Initialize ImageView
        postCaption = findViewById(R.id.postCaption) // Initialize caption TextView
        seeMoreButton = findViewById(R.id.seeMoreButton) // Initialize See More Button
        likeCount = findViewById(R.id.likeCount) // Initialize likes TextView
        commentCount = findViewById(R.id.commentCount) // Initialize comments TextView
        shareCount = findViewById(R.id.shareCount) // Initialize shares TextView
        playerView = findViewById(R.id.videoPlayerView) // Initialize ExoPlayer view

        // Set the back button click listener
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Retrieve data from intent
        val mediaContent = intent.getStringExtra("MEDIA")
        val caption = intent.getStringExtra("Caption")
        val likes = intent.getIntExtra("Likes", 0)
        val comments = intent.getIntExtra("Comments", 0)
        val shares = intent.getIntExtra("Shares", 0)
        val trimmedAudioUrl = intent.getStringExtra("trimAudio")
        val createdAt = intent.getStringExtra("createdAt")

        Log.d("ViewPost", "Media: $mediaContent, Caption: $caption, Likes: $likes, Comments: $comments, Shares: $shares, Trimmed Audio: $trimmedAudioUrl, Created At: $createdAt")

        // Set the retrieved data to views
        likeCount.text = likes.toString() // Set likes count
        commentCount.text = comments.toString() // Set comments count
        shareCount.text = shares.toString() // Set shares count

        // Handle caption display
        if (caption.isNullOrEmpty() || caption == "null") {
            postCaption.visibility = View.GONE // Hide if null or empty
            seeMoreButton.visibility = View.GONE // Hide the button if there's no caption
            Log.d("ViewPost", "Caption is null or empty, hiding caption view.")
        } else {
            postCaption.text = if (caption.length > 50) {
                caption.take(50) + "..." // Display only the first 50 characters with ellipsis
            } else {
                caption // Display the whole caption if it's within the limit
            }
            seeMoreButton.visibility = if (caption.length > 50) View.VISIBLE else View.GONE // Show See More button if necessary
            Log.d("ViewPost", "Caption set to: ${postCaption.text}")
        }

        // Set up See More button listener
        seeMoreButton.setOnClickListener {
            toggleCaption() // Toggle caption visibility
        }

        mediaContent?.let {
            loadMedia(it) // Load media content
        } ?: Log.d("ViewPost", "Media content is null.")

        // Play the trimmed audio if available
        trimmedAudioUrl?.let {
            playTrimmedAudio(it) // Play trimmed audio
        } ?: Log.d("ViewPost", "Trimmed audio URL is null.")
    }

    private fun loadMedia(mediaUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val mediaType = getMediaType(mediaUrl)

            // Switch back to the main thread to update UI
            withContext(Dispatchers.Main) {
                if (mediaType != null) {
                    when {
                        mediaType.startsWith("image/") -> {
                            imageView.visibility = View.VISIBLE
                            playerView.visibility = View.GONE
                            setImage(mediaUrl)
                        }
                        mediaType.startsWith("video/mp4") -> {
                            imageView.visibility = View.GONE
                            playerView.visibility = View.VISIBLE
                            playVideo(mediaUrl)
                        }
                        else -> {
                            Log.e("ViewPost", "Unsupported media type: $mediaType")
                        }
                    }
                } else {
                    Log.e("ViewPost", "Failed to retrieve media type.")
                }
            }
        }
    }

    private suspend fun getMediaType(mediaUrl: String): String? {
        val request = Request.Builder()
            .url(mediaUrl)
            .head() // Use HEAD to get the headers only
            .build()

        return try {
            val response: Response = client.newCall(request).execute()
            // Handle response and return media type
            response.header("Content-Type").also {
                Log.d("ViewPost", "Media type retrieved: $it")
            }
        } catch (e: Exception) {
            Log.e("ViewPost", "Error retrieving media type: ${e.message}")
            null
        }
    }

    private fun setImage(imageUrl: String) {
        try {
            val uri = Uri.parse(imageUrl)
            Glide.with(this)
                .load(uri)
                .thumbnail(0.1f) // Show a thumbnail while loading
                .error(R.mipmap.appicon2) // Show a default error image if loading fails
                .into(imageView)
            Log.d("ViewPost", "Attempting to load image from URL: $imageUrl")

        } catch (e: Exception) {
            Log.e("ViewPost", "Exception while loading image: ${e.message}")
        }
    }

    private fun toggleCaption() {
        isExpanded = !isExpanded // Toggle the expanded state
        postCaption.text = if (isExpanded) {
            intent.getStringExtra("Caption") // Display full caption
        } else {
            intent.getStringExtra("Caption")?.take(50) + "..." // Limit to 50 characters again
        }
        seeMoreButton.text = if (isExpanded) "See Less" else "See More" // Change button text
    }

    private fun playVideo(videoPath: String) {
        val mediaItem = MediaItem.fromUri(Uri.parse(videoPath))
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true // Start playback immediately
            playerView.player = this
        }

        Log.d("ViewPost", "Attempting to play video from path: $videoPath")
    }

    private fun playTrimmedAudio(outputPath: String) {
        // You can also use ExoPlayer to play audio, here’s an example for that:
        val audioUri = Uri.parse(outputPath)
        val audioMediaItem = MediaItem.fromUri(audioUri)
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setMediaItem(audioMediaItem)
            prepare()
            playWhenReady = true
        }

        Log.d("ViewPost", "Playing trimmed audio from: $outputPath")
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onBackPressed() {
        exoPlayer?.release()
        super.onBackPressed() // Call super to finish the activity
    }
}
