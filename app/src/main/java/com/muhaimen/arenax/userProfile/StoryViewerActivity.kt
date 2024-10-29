package com.muhaimen.arenax.userProfile

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.StoryWithTimeAgo

class StoryViewActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var timeAgoTextView: TextView
    private lateinit var currentStory: StoryWithTimeAgo
    private var storiesList: List<StoryWithTimeAgo> = listOf()
    private var currentIndex = 0

    private val handler = Handler()
    private var mediaPlayer: MediaPlayer? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_viewer)

        videoView = findViewById(R.id.videoView)
        imageView = findViewById(R.id.imageView)
        progressBar = findViewById(R.id.progressBar)
        timeAgoTextView = findViewById(R.id.timeAgoTextView)

        // Get the stories from the intent
        storiesList = intent.getParcelableArrayListExtra("storiesList") ?: listOf()
        currentIndex = intent.getIntExtra("currentIndex", 0)

        if (storiesList.isNotEmpty()) {
            displayStory()
        }

        // Handle touch events to navigate stories
        findViewById<View>(R.id.storyViewContainer).setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (event.x > resources.displayMetrics.widthPixels / 2) {
                        // Next story
                        nextStory()
                    } else {
                        // Previous story
                        previousStory()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun displayStory() {
        currentStory = storiesList[currentIndex]

        // Set the "hours ago" text
        timeAgoTextView.text = "${currentStory.hoursAgo} hours ago"

        // Set the progress bar
        progressBar.max = storiesList.size
        progressBar.progress = currentIndex + 1 // Progress is 1-based

        if (currentStory.story.mediaUrl.endsWith(".mp4")) {
            // Load video
            videoView.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            videoView.setVideoPath(currentStory.story.mediaUrl)
            videoView.start()

            // Optional audio handling
            if (currentStory.story.trimmedAudioUrl != null) {
                mediaPlayer = MediaPlayer.create(this, Uri.parse(currentStory.story.trimmedAudioUrl))
                mediaPlayer?.start()
            }

            // Move to next story after duration
            handler.postDelayed({ nextStory() }, currentStory.story.duration.toLong())
        } else {
            // Load image
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE

            Glide.with(this)
                .load(currentStory.story.mediaUrl)
                .into(imageView) // Load the image

            // Move to next story after duration
            handler.postDelayed({ nextStory() }, currentStory.story.duration.toLong())
        }
    }

    private fun nextStory() {
        // Stop any ongoing media
        stopMedia()

        if (currentIndex < storiesList.size - 1) {
            currentIndex++
            displayStory()
        } else {
            finish() // No more stories
        }
    }

    private fun previousStory() {
        // Stop any ongoing media
        stopMedia()

        if (currentIndex > 0) {
            currentIndex--
            displayStory()
        }
    }

    private fun stopMedia() {
        mediaPlayer?.release()
        mediaPlayer = null
        videoView.stopPlayback()
        handler.removeCallbacksAndMessages(null) // Remove any pending callbacks
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMedia()
    }
}
