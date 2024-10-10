package com.muhaimen.arenax.uploadContent

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.TextureView
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
    private lateinit var textureView: TextureView // TextureView for playing video
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()
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

        // Initialize views
        backButton = findViewById(R.id.backButton)
        imageView = findViewById(R.id.ImageView) // Initialize ImageView
        postCaption = findViewById(R.id.postCaption) // Initialize caption TextView
        seeMoreButton = findViewById(R.id.seeMoreButton) // Initialize See More Button
        likeCount = findViewById(R.id.likeCount) // Initialize likes TextView
        commentCount = findViewById(R.id.commentCount) // Initialize comments TextView
        shareCount = findViewById(R.id.shareCount) // Initialize shares TextView
        textureView = findViewById(R.id.VideoView) // Initialize TextureView

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
                            textureView.visibility = View.GONE
                            setImage(mediaUrl)
                        }
                        mediaType.startsWith("video/mp4") -> {
                            imageView.visibility = View.GONE
                            textureView.visibility = View.VISIBLE
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

                    // Add buffering listeners
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


    private fun playTrimmedAudio(outputPath: String) {
        val uri = Uri.parse(outputPath)
        mediaPlayer = MediaPlayer()

        mediaPlayer?.apply {
            try {
                setDataSource(applicationContext, uri)
                prepare()
                start()
                Log.d("ViewPost", "Playing trimmed audio from: $outputPath")

                setOnCompletionListener {
                    Log.d("ViewPost", "Trimmed audio playback completed.")
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e("ViewPost", "Error occurred while playing trimmed audio. What: $what, Extra: $extra")
                    true
                }

            } catch (e: IOException) {
                Log.e("ViewPost", "Error setting data source for audio: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
