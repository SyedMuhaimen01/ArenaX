package com.muhaimen.arenax.userFeed

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.ViewAllChats
import com.muhaimen.arenax.notifications.Notifications
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.explore.ExplorePage

class UserFeed : AppCompatActivity() {
    private lateinit var userFeedAdapter: UserFeedPostsAdapter
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var threadsButton: ImageButton
    private lateinit var notificationsButton: ImageButton
    private lateinit var homeButton: LinearLayout
    private lateinit var addPost: ImageView
    private lateinit var profileButton: ImageView
    private lateinit var exploreButton: ImageView

    // Store the selected post to manage comments section
    private var selectedPost: DummyPost? = null

    // Track visibility of comments section
    private var isCommentsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_feed)

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNavigation()

        // Set status bar and navigation bar color
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        threadsButton = findViewById(R.id.threadsButton)
        threadsButton.setOnClickListener {
            val intent = Intent(this, ViewAllChats::class.java)
            startActivity(intent)
        }

        notificationsButton = findViewById(R.id.notificationsButton)
        notificationsButton.setOnClickListener {
            val intent = Intent(this, Notifications::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView for user feed
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewUserFeed)
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Create dummy data
        val dummyPosts = createDummyPosts()

        // Initialize the adapter with dummy data and click listeners
        userFeedAdapter = UserFeedPostsAdapter(
            dummyPosts,
            onLikeClick = { post -> onLikePost(post) },
            onShareClick = { post -> onSharePost(post) },
            onCommentClick = { post -> onCommentPost(post) }
        )

        // Set the adapter to the RecyclerView
        recyclerView.adapter = userFeedAdapter
    }

    // Handle like action
    private fun onLikePost(post: DummyPost) {
        println("${post.userName}'s post liked")
    }

    // Handle share action
    private fun onSharePost(post: DummyPost) {
        println("Share ${post.userName}'s post")
    }

    // Handle comment action and toggle the visibility of the comments section
    private fun onCommentPost(post: DummyPost) {
        // Save the selected post to manage its comments section
        selectedPost = post

        // Initialize the CommentsAdapter for this post's comments
        val recyclerViewComments: RecyclerView = findViewById(R.id.commentsRecyclerView)

    }

    // Create dummy posts with comments
    private fun createDummyPosts(): List<DummyPost> {
        return listOf(
            DummyPost(
                postContent = "https://firebasestorage.googleapis.com/v0/b/arenax-e1289.appspot.com/o/uploads%2F22a17c9e-7182-440a-b396-dc3fa3c93738?alt=media&token=d04ed26c-cc16-4afd-bad4-28934dac8a4e",
                userName = "User1",
                contentText = "This is a dummy text post.",
                location = "location",
                profilePictureUrl ="R.drawable.game_icon_foreground ",
                comments = mutableListOf("Nice post!", "Great game!", "I agree!", "Well said", "I love this game!", "Great game!", "I agree!", "Well said", "I love this game!")
            ),
            DummyPost(
                postContent = "https://firebasestorage.googleapis.com/v0/b/arenax-e1289.appspot.com/o/uploads%2F22a17c9e-7182-440a-b396-dc3fa3c93738?alt=media&token=d04ed26c-cc16-4afd-bad4-28934dac8a4e",
                userName = "User2",
                location = "location",
                contentText = "This is a dummy text post.",
                profilePictureUrl ="R.drawable.game_icon_foreground ",
                comments = mutableListOf("Looks amazing!", "Love this game!")
            ),
            DummyPost(
                postContent = "https://firebasestorage.googleapis.com/v0/b/arenax-e1289.appspot.com/o/uploads%2F2041a0ec-5302-4511-9d42-8acf841686ae?alt=media&token=371d4abe-6a24-4d08-aa88-476f8ab90cac",
                userName = "User3",
                location = "location",
                contentText = "This is a dummy text post.",
                profilePictureUrl ="R.drawable.game_icon_foreground ",
                comments = mutableListOf("Awesome gameplay!", "This is epic!")
            )
        )
    }

    // Setup Bottom Navigation buttons
    private fun setupBottomNavigation() {
        homeButton = findViewById(R.id.home)
        homeButton.setOnClickListener {
            val intent = Intent(this, UserFeed::class.java)
            startActivity(intent)
        }

        addPost = findViewById(R.id.addPostButton)
        addPost.setOnClickListener {
            val intent = Intent(this, UploadContent::class.java)
            startActivity(intent)
        }

        profileButton = findViewById(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }

        exploreButton = findViewById(R.id.exploreButton)
        exploreButton.setOnClickListener {
            val intent = Intent(this, ExplorePage::class.java)
            startActivity(intent)
        }
    }
}
