package com.muhaimen.arenax.userProfile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.series.DataPoint
import com.muhaimen.arenax.R
import com.muhaimen.arenax.editProfile.editProfile

class UserProfile : AppCompatActivity() {

    private lateinit var analyticsRecyclerView: RecyclerView
    private lateinit var analyticsAdapter: AnalyticsAdapter

    private lateinit var highlightsRecyclerView: RecyclerView
    private lateinit var highlightsAdapter: HighlightsAdapter

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter

    private lateinit var bioTextView: TextView
    private lateinit var showMoreTextView: TextView
   private lateinit var editProfileButton: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        // Set window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the TextViews
        bioTextView = findViewById(R.id.bioText)
        showMoreTextView = findViewById(R.id.showMore)

        val bio = "This is a long bio that might need to be shortened to fit on the screen. Here is some more content that will be hidden initially.This is a long bio that might need to be shortened to fit on the screen. Here is some more content that will be hidden initially."
        bioTextView.text = bio

        // Check the length of the bio text to determine if "See More" should be shown
        if (bio.length > 50) { // Adjust the character count as needed
            showMoreTextView.visibility = View.VISIBLE
        }

        showMoreTextView.setOnClickListener {
            // Expand bio to show full text
            bioTextView.maxLines = Int.MAX_VALUE
            bioTextView.ellipsize = null
            showMoreTextView.visibility = View.GONE  // Hide "See More"
        }

        // Initialize the RecyclerView for analytics
        analyticsRecyclerView = findViewById(R.id.analytics_recyclerview)
        analyticsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Load sample data into the analytics adapter
        val sampleData = loadSampleAnalyticsData()
        analyticsAdapter = AnalyticsAdapter(sampleData)
        analyticsRecyclerView.adapter = analyticsAdapter

        // Initialize the RecyclerView for highlights
        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Load sample data into the highlights adapter
        val sampleHighlights = loadSampleHighlightsData()
        highlightsAdapter = HighlightsAdapter(sampleHighlights)
        highlightsRecyclerView.adapter = highlightsAdapter

        // Initialize the RecyclerView for posts
        postsRecyclerView = findViewById(R.id.posts_recyclerview)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)

        // Load sample data into the posts adapter
        val samplePosts = loadSamplePostsData()
        postsAdapter = PostsAdapter(samplePosts)
        postsRecyclerView.adapter = postsAdapter

        // Initialize the Edit Profile button
        editProfileButton= findViewById(R.id.editProfileButton)
        editProfileButton.setOnClickListener {
            val intent = Intent(this, editProfile::class.java)
            startActivity(intent)
        }
    }

    // Sample function to load analytics data
    private fun loadSampleAnalyticsData(): List<AnalyticsData> {
        // Example data points for graph (Hours vs Days)
        val hoursData1 = listOf(
            DataPoint(1.0, 2.0),
            DataPoint(2.0, 3.0),
            DataPoint(3.0, 5.0),
            DataPoint(4.0, 7.0),
            DataPoint(5.0, 4.0)
        )
        val hoursData2 = listOf(
            DataPoint(1.0, 1.0),
            DataPoint(2.0, 2.5),
            DataPoint(3.0, 4.5),
            DataPoint(4.0, 6.0),
            DataPoint(5.0, 5.0)
        )

        // Create AnalyticsData instances
        val game1 = AnalyticsData(
            gameName = "Game 1",
            totalHours = 15,
            iconResId = R.drawable.game_icon_foreground,
            hoursData = hoursData1
        )

        val game2 = AnalyticsData(
            gameName = "Game 2",
            totalHours = 20,
            iconResId = R.drawable.game_icon_foreground,
            hoursData = hoursData2
        )

        // Return a list of analytics data
        return listOf(game1, game2)
    }

    // Sample function to load highlights data
    private fun loadSampleHighlightsData(): List<Highlight> {
        return listOf(
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 1"),
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 2"),
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 3"),
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 4"),
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 5")
        )
    }

    // Sample function to load posts data
    private fun loadSamplePostsData(): List<Post> {
        return listOf(
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground)
        )
    }


}
