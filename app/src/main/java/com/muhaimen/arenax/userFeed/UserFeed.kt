package com.muhaimen.arenax.userFeed

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.ViewAllChats
import com.muhaimen.arenax.explore.ExplorePage
import com.muhaimen.arenax.notifications.Notifications
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.userProfile.UserProfile

class UserFeed : AppCompatActivity() {
    private lateinit var userFeedAdapter: UserFeedPostsAdapter
    private lateinit var commentsAdapter: CommentsAdapter // Add the comments adapter
    private lateinit var threadsButton: ImageButton
    private lateinit var notificationsButton: ImageButton
    private lateinit var homeButton: LinearLayout
    private lateinit var addPost: ImageView
    private lateinit var profileButton: ImageView
    private lateinit var exploreButton: ImageView

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
    @SuppressLint("ResourceType")
    private fun onCommentPost(post: DummyPost) {
        val commentInputLayout: ConstraintLayout = findViewById(R.layout.userfeed_comments_card) // Your comment card layout
        val editTextComment: EditText = commentInputLayout.findViewById(R.id.editTextComment)
        val buttonPostComment: Button = commentInputLayout.findViewById(R.id.buttonPostComment)

        // Show the comment input layout and button
        editTextComment.visibility = View.VISIBLE
        buttonPostComment.visibility = View.VISIBLE

        // Set a listener for the "Post" button
        buttonPostComment.setOnClickListener {
            val newComment = editTextComment.text.toString()
            if (newComment.isNotEmpty()) {
                post.comments.add(newComment) // Add the comment to the post's comments
                userFeedAdapter.notifyDataSetChanged() // Update the adapter to reflect the new comment
                editTextComment.text.clear() // Clear the input field
                // Hide the comment input layout again
                editTextComment.visibility = View.GONE
                buttonPostComment.visibility = View.GONE
            }
        }
    }

    // Handle share action
    private fun onSharePost(post: DummyPost) {
        // Implement the action when a post is shared
        println("Share ${post.userName}'s post")
    }

    // Create dummy posts with comments
    private fun createDummyPosts(): List<DummyPost> {
        return listOf(
            DummyPost(
                profilePictureUrl = "R.drawable.game_icon_foreground",
                userName = "User1",
                contentType = "text",
                contentText = "This is a dummy text post.",
                comments = mutableListOf("Nice post!", "Great game!", "I agree!","well said","I love this game!")
            ),
            DummyPost(
                profilePictureUrl = "R.drawable.game_icon_foreground",
                userName = "User2",
                contentType = "image",
                contentImageUrl = "R.drawable.game_icon_foreground",
                comments = mutableListOf("Looks amazing!", "Love this game!")
            ),
            DummyPost(
                profilePictureUrl = "R.drawable.game_icon_foreground",
                userName = "User3",
                contentType = "video",
                contentVideoUrl = "https://firebasestorage.googleapis.com/v0/b/arenax-e1289.appspot.com/o/uploads%2F3c5af154-c52f-4fd9-a96f-1e3f4007869f?alt=media&token=3f42ce07-aa96-491b-a53d-b82454bb3946",
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
