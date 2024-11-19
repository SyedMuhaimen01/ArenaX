package com.muhaimen.arenax.Threads

import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors

class viewChatMedia : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var videoView: VideoView
    private lateinit var backButton: ImageButton
    private lateinit var downloadButton: ImageButton
    private var isVideoPlaying = true
    private val PERMISSION_REQUEST_CODE = 1001 // Define permission request code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_chat_media)

        // Initialize views
        imageView = findViewById(R.id.fullSizeImage)
        videoView = findViewById(R.id.fullSizeVideo)
        backButton = findViewById(R.id.backButton)
        downloadButton = findViewById(R.id.downloadButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        // Get the media URL and type from intent
        val mediaUrl = intent.getStringExtra("mediaUrl")
        val mediaType = intent.getStringExtra("mediaType")

        if (mediaUrl != null && mediaType != null) {
            if (mediaType == "image") {
                loadImage(mediaUrl)
            } else if (mediaType == "video") {
                loadVideo(mediaUrl)
            }
        }

        // Back button functionality
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Download button functionality
        downloadButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            } else {
                downloadMedia(mediaUrl, mediaType)
            }
        }
    }

    private fun loadImage(url: String) {
        imageView.visibility = ImageView.VISIBLE
        videoView.visibility = VideoView.GONE
        Glide.with(this).load(url).into(imageView)
    }

    private fun loadVideo(url: String) {
        imageView.visibility = ImageView.GONE
        videoView.visibility = VideoView.VISIBLE
        videoView.setVideoURI(Uri.parse(url))
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            videoView.start() // Start the video as soon as it's prepared
        }
        videoView.setOnClickListener {
            if (isVideoPlaying) {
                videoView.pause()
            } else {
                videoView.start()
            }
            isVideoPlaying = !isVideoPlaying
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, proceed with media download
                val mediaUrl = intent.getStringExtra("mediaUrl")
                val mediaType = intent.getStringExtra("mediaType")
                downloadMedia(mediaUrl, mediaType)
            } else {
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Closes the current activity
    }

    private fun downloadMedia(url: String?, mediaType: String?) {
        if (url == null || mediaType == null) {
            Toast.makeText(this, "Unable to download media", Toast.LENGTH_SHORT).show()
            return
        }

        Executors.newSingleThreadExecutor().execute {
            try {
                // Determine file name based on media type
                val fileName = if (mediaType == "image") "downloaded_image_${System.currentTimeMillis()}.jpg" else "downloaded_video_${System.currentTimeMillis()}.mp4"

                // Open input stream from URL
                val inputStream: InputStream = URL(url).openStream()

                // Prepare output stream and file for the gallery
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, if (mediaType == "image") "image/jpeg" else "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, if (mediaType == "image") Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES)
                }

                // Insert into the MediaStore
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        inputStream.use { input ->
                            input.copyTo(outputStream)
                        }
                    }
                    runOnUiThread {
                        Toast.makeText(this, "Media downloaded to gallery", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    throw Exception("Failed to create MediaStore entry.")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }
}
