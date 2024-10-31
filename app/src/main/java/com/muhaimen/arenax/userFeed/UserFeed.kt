package com.muhaimen.arenax.userFeed

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.ViewAllChats

class UserFeed : AppCompatActivity() {
    private lateinit var userFeedAdapter: UserFeedPostsAdapter
    private lateinit var threadsButton: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_feed)

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set status bar and navigation bar color
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        threadsButton = findViewById(R.id.threadsButton)
        threadsButton.setOnClickListener {
            val intent = Intent(this, ViewAllChats::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView and Adapter
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewUserFeed)
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Create dummy data
        val dummyPosts = createDummyPosts()

        // Initialize the adapter with dummy data and click listeners
        userFeedAdapter = UserFeedPostsAdapter(
            dummyPosts,
            onLikeClick = { post -> onLikePost(post) },
            onCommentClick = { post -> onCommentPost(post) },
            onShareClick = { post -> onSharePost(post) }
        )

        // Set the adapter to the RecyclerView
        recyclerView.adapter = userFeedAdapter
    }

    // Handle like action
    private fun onLikePost(post: DummyPost) {
        // Implement the action when a post is liked
        println("${post.userName}'s post liked")
    }

    // Handle comment action
    private fun onCommentPost(post: DummyPost) {
        // Implement the action when a post is commented on
        println("Comment on ${post.userName}'s post")
    }

    // Handle share action
    private fun onSharePost(post: DummyPost) {
        // Implement the action when a post is shared
        println("Share ${post.userName}'s post")
    }

    // Create dummy posts
    private fun createDummyPosts(): List<DummyPost> {
        return listOf(
            DummyPost(
                profilePictureUrl = "R.drawable.game_icon_foreground",
                userName = "User1",
                contentType = "text",
                contentText = "This is a dummy text post."
            ),
            DummyPost(
                profilePictureUrl = "R.drawable.game_icon_foreground",
                userName = "User2",
                contentType = "image",
                contentImageUrl = "R.drawable.game_icon_foreground"
            ),DummyPost(
                profilePictureUrl = "R.drawable.game_icon_foreground",
                userName = "User3",
                contentType = "image",
                contentImageUrl = "R.drawable.game_icon_foreground"
            )
            // Add more DummyPost instances as needed
        )
    }
}
