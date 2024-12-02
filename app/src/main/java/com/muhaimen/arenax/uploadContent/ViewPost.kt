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
    private lateinit var postCaption: TextView
    private lateinit var seeMoreButton: Button
    private lateinit var likeCount: TextView
    private lateinit var commentCount: TextView
    private lateinit var shareCount: TextView
    private lateinit var textureView: TextureView
    private var mediaPlayer: MediaPlayer? = null

    private val client = OkHttpClient()
    private var isExpanded: Boolean = false

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
        imageView = findViewById(R.id.ImageView)
        postCaption = findViewById(R.id.postCaption)
        seeMoreButton = findViewById(R.id.seeMoreButton)
        likeCount = findViewById(R.id.likeCount)
        commentCount = findViewById(R.id.commentCount)
        shareCount = findViewById(R.id.shareCount)
        textureView = findViewById(R.id.VideoView)

        // Set back button listener
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Retrieve post data from intent
        val mediaContent = intent.getStringExtra("MEDIA")
        val caption = intent.getStringExtra("Caption")
        val likes = intent.getIntExtra("Likes", 0)
        val comments = intent.getIntExtra("Comments", 0)
        val shares = intent.getIntExtra("Shares", 0)
        val trimmedAudioUrl = intent.getStringExtra("trimAudio")
        val createdAt = intent.getStringExtra("createdAt")

        // Set text data to UI components
        likeCount.text = likes.toString()
        commentCount.text = comments.toString()
        shareCount.text = shares.toString()

        // Handle caption
        if (caption.isNullOrEmpty() || caption == "null") {
            postCaption.visibility = View.GONE
            seeMoreButton.visibility = View.GONE
        } else {
            postCaption.text = if (caption.length > 50) {
                caption.take(50) + "..."
            } else {
                caption
            }
            seeMoreButton.visibility = if (caption.length > 50) View.VISIBLE else View.GONE
        }

        // Set See More button listener
        seeMoreButton.setOnClickListener {
            toggleCaption()
        }

        // Load media (image, video, or audio)
        mediaContent?.let {
            loadMedia(it)
        }

        // Play trimmed audio if available
        trimmedAudioUrl?.let {
            playTrimmedAudio(it)
        }
    }

    private fun loadMedia(mediaUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val mediaType = getMediaType(mediaUrl)

            // Update UI on the main thread
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
            .head() // Use HEAD request to fetch only headers
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
        try {
            val uri = Uri.parse(imageUrl)
            Glide.with(this)
                .load(uri)
                .thumbnail(0.1f) // Thumbnail loading
                .error(R.mipmap.appicon2) // Default error image
                .into(imageView)
        } catch (e: Exception) {
            Log.e("ViewPost", "Exception loading image: ${e.message}")
        }
    }

    private fun toggleCaption() {
        isExpanded = !isExpanded
        postCaption.text = if (isExpanded) {
            intent.getStringExtra("Caption") // Show full caption
        } else {
            intent.getStringExtra("Caption")?.take(50) + "..." // Truncate after 50 characters
        }
        seeMoreButton.text = if (isExpanded) "See Less" else "See More"
    }

    private fun playVideo(videoPath: String) {
        val uri = Uri.parse(videoPath)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(videoPath)
                    setSurface(Surface(surface))
                    prepareAsync()
                    setOnPreparedListener {
                        isLooping = true
                        start()
                    }
                    setOnCompletionListener {
                        seekTo(0)
                        start()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("ViewPost", "Error: What=$what, Extra=$extra")
                        true
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
            } catch (e: IOException) {
                Log.e("ViewPost", "Error playing audio: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBackPressed() {
        mediaPlayer?.release()
        super.onBackPressed()
    }
}
