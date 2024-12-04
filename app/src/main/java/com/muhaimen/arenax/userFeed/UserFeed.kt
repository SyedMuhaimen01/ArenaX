package com.muhaimen.arenax.userFeed

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.ViewAllChats
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.notifications.Notifications
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.explore.ExplorePage
import com.muhaimen.arenax.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class UserFeed : AppCompatActivity() {
    private lateinit var userFeedAdapter: UserFeedPostsAdapter
    private lateinit var commentsAdapter: commentsAdapter
    private lateinit var threadsButton: ImageButton
    private lateinit var notificationsButton: ImageButton
    private lateinit var homeButton: LinearLayout
    private lateinit var addPost: ImageView
    private lateinit var profileButton: ImageView
    private lateinit var exploreButton: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val postsList = mutableListOf<Post>()

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
        database = FirebaseDatabase.getInstance().reference
        auth= FirebaseAuth.getInstance()
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

        // Initialize adapter with empty data
        userFeedAdapter = UserFeedPostsAdapter(
            postsList,
            onLikeClick = { post -> onLikePost(post) },
            onShareClick = { post -> onSharePost(post) },
            onCommentClick = { post -> onCommentPost(post) }
        )
        recyclerView.adapter = userFeedAdapter

        // Fetch and populate posts from backend
        fetchFollowingAndPopulatePosts()
    }

    private fun fetchFollowingAndPopulatePosts() {
        lifecycleScope.launch {
            try {
                // Call the combined function
                fetchUserFeed { posts ->
                    // Update postsList and notify adapter
                    postsList.clear()
                    postsList.addAll(posts)
                    userFeedAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("UserFeed", "Error fetching posts", e)
                Toast.makeText(this@UserFeed, "Failed to fetch posts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserFeed(onPostsFetched: (List<Post>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val followingRef = database.child("userData").child(userId).child("synerG").child("following")

        followingRef.orderByChild("status").equalTo("accepted").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val followingUids = snapshot.children.mapNotNull { it.key }

                    if (followingUids.isNotEmpty()) {
                        // Prepare the request to fetch posts
                        val url = "${Constants.SERVER_URL}explorePosts/user/$userId/fetchFeed"
                        val client = OkHttpClient()

                        val requestBody = JSONObject().apply {
                            put("followingIds", JSONArray(followingUids))
                        }.toString().toRequestBody("application/json".toMediaType())

                        val request = Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build()

                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val response = client.newCall(request).execute()
                                if (response.isSuccessful) {
                                    val responseData = response.body?.string()
                                    if (!responseData.isNullOrEmpty()) {
                                        val posts = parsePostsFromResponse(responseData)
                                        withContext(Dispatchers.Main) {
                                            onPostsFetched(posts)
                                        }
                                    } else {
                                        Log.e("fetchUserFeed", "Empty response from server")
                                    }
                                } else {
                                    Log.e("fetchUserFeed", "Server error: ${response.code}")
                                }
                            } catch (e: Exception) {
                                Log.e("fetchUserFeed", "Error fetching posts", e)
                            }
                        }
                    } else {
                        Log.d("fetchUserFeed", "No following users found")
                    }
                } else {
                    Log.d("fetchUserFeed", "No data exists for following users")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("fetchUserFeed", "Error fetching following list: ${exception.message}")
            }
    }

    // Helper function to parse posts
    private fun parsePostsFromResponse(responseData: String): List<Post> {
        val posts = mutableListOf<Post>()
        val jsonArray = JSONArray(responseData)

        for (i in 0 until jsonArray.length()) {
            val postObject = jsonArray.getJSONObject(i)

            val commentsData = mutableListOf<Comment>()
            val commentsArray = postObject.optJSONArray("comments")
            commentsArray?.let {
                for (j in 0 until it.length()) {
                    val commentObject = it.getJSONObject(j)
                    val comment = Comment(
                        commentId = commentObject.optInt("comment_id", 0),
                        commentText = commentObject.optString("comment", ""),
                        createdAt = commentObject.optString("created_at", ""),
                        commenterName = commentObject.optString("commenter_name", "Unknown"),
                        commenterProfilePictureUrl = commentObject.optString("commenter_profile_pic", null)
                    )
                    commentsData.add(comment)
                }
            }

            val post = Post(
                postId = postObject.optInt("post_id", 0),
                postContent = postObject.optString("post_content", null),
                caption = postObject.optString("caption", null),
                sponsored = postObject.optBoolean("sponsored", false),
                likes = postObject.optInt("likes", 0),
                comments = postObject.optInt("post_comments", 0),
                shares = postObject.optInt("shares", 0),
                clicks = postObject.optInt("clicks", 0),
                city = postObject.optString("city", null),
                country = postObject.optString("country", null),
                trimmedAudioUrl = postObject.optString("trimmed_audio_url", null),
                createdAt = postObject.optString("created_at", ""),
                userFullName = postObject.optString("full_name", "Unknown User"),
                userProfilePictureUrl = postObject.optString("profile_picture_url", null),
                commentsData = commentsData
            )
            posts.add(post)
        }

        return posts
    }



    // Handle like action
    private fun onLikePost(post: Post) {
        println("${post.userFullName}'s post liked")
    }

    // Handle share action
    private fun onSharePost(post: Post) {
        println("Share ${post.userFullName}'s post")
    }

    // Handle comment action
    private fun onCommentPost(post: Post) {
        println("Comment on ${post.userFullName}'s post")
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
