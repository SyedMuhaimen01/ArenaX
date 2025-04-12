package com.muhaimen.arenax.userFeed

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import com.muhaimen.arenax.dataClasses.Story
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.notifications.Notifications
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.explore.ExplorePage
import com.muhaimen.arenax.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class UserFeed : AppCompatActivity() {
    private lateinit var userFeedAdapter: UserFeedPostsAdapter
    private lateinit var highlightsAdapter: UserFeedHighlightsAdapter
    private lateinit var highlightsRecyclerView: RecyclerView
    lateinit var recyclerView: RecyclerView
    private lateinit var threadsButton: ImageButton
    private lateinit var notificationsButton: ImageButton
    private lateinit var homeButton: LinearLayout
    private lateinit var addPost: FrameLayout
    private lateinit var profileButton: LinearLayout
    private lateinit var exploreButton: LinearLayout
    private lateinit var talentExchangeButton:LinearLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val postsList = mutableListOf<Post>()
    private val storiesList = mutableListOf<Story>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_feed)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

     /*   val textView = findViewById<TextView>(R.id.x_text)

        val paint = textView.paint
        val width = paint.measureText(textView.text.toString())

        val textShader = LinearGradient(
            0f, 0f, width, textView.textSize*1,
            intArrayOf(
                resources.getColor(R.color.gradientStartOrange, null),
                resources.getColor(R.color.gradientEndYellow, null)
            ),
            null,
            Shader.TileMode.CLAMP
        )

        textView.paint.shader = textShader*/

        setupBottomNavigation()

        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        database = FirebaseDatabase.getInstance().reference
        auth= FirebaseAuth.getInstance()
        threadsButton = findViewById(R.id.threadsButton)
        threadsButton.setOnClickListener {
            userFeedAdapter.releaseAllPlayers()
            val intent = Intent(this, ViewAllChats::class.java)
            startActivity(intent)
        }

        notificationsButton = findViewById(R.id.notificationsButton)
        notificationsButton.setOnClickListener {
            userFeedAdapter.releaseAllPlayers()
            val intent = Intent(this, Notifications::class.java)
            startActivity(intent)
        }



        // Initialize RecyclerView for user feed
        recyclerView = findViewById(R.id.recyclerViewUserFeed)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty data
        userFeedAdapter = UserFeedPostsAdapter(fragmentManager = supportFragmentManager,recyclerView,postsList)
        recyclerView.adapter = userFeedAdapter

        highlightsRecyclerView = findViewById(R.id.highlightsRecyclerView)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        highlightsAdapter = UserFeedHighlightsAdapter(storiesList)
        highlightsRecyclerView.adapter = highlightsAdapter

        // Fetch and populate UserFeed
        fetchFollowingAndPopulatePosts()
        fetchFollowedUsersStories()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                userFeedAdapter.handlePlayerVisibility()
            }
        })
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun fetchFollowingAndPopulatePosts() {
        lifecycleScope.launch {
            try {
                fetchUserFeed { posts ->
                    postsList.clear()
                    postsList.addAll(posts)
                    userFeedAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("UserFeed", "Error fetching posts", e)
            }
        }
    }

    private fun fetchUserFeed(onPostsFetched: (List<Post>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val followingRef = database.child("userData").child(userId).child("synerG").child("following")

        followingRef.orderByChild("status").equalTo("accepted").get()
            .addOnSuccessListener { snapshot ->
                val followingUids = snapshot.children.mapNotNull { it.key }
                val url = "${Constants.SERVER_URL}explorePosts/user/$userId/fetchFeedPosts"
                val client = OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS) // Timeout for establishing a connection
                    .readTimeout(180, TimeUnit.SECONDS)    // Timeout for reading data from the server
                    .writeTimeout(120, TimeUnit.SECONDS)   // Timeout for writing data to the server
                    .build()
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

            // Post object
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
                commentsData = commentsData,
                isLikedByUser = postObject.optBoolean("likedByUser", false)  // Default to false if not provided
            )
            posts.add(post)
        }

        return posts
    }

    private fun fetchFollowedUsersStories() {
        val userId = auth.currentUser?.uid ?: return
        val followingRef = database.child("userData").child(userId).child("synerG").child("following")

        followingRef.orderByChild("status").equalTo("accepted").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val followingUids = snapshot.children.mapNotNull { it.key }

                    if (followingUids.isNotEmpty()) {
                        val url = "${Constants.SERVER_URL}stories/user/$userId/fetchFeedStories"
                        val client = OkHttpClient()

                        val requestBody = JSONObject().apply {
                            put("followingUids", JSONArray(followingUids))
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
                                        val storiesList = parseStoriesFromResponse(responseData)
                                        withContext(Dispatchers.Main) {
                                            updateStoriesUI(storiesList)
                                        }
                                    } else {
                                        Log.e("fetchFollowedUsersStories", "Empty response from server")
                                    }
                                } else {
                                    Log.e("fetchFollowedUsersStories", "Server error: ${response.code}")
                                }
                            } catch (e: Exception) {
                                Log.e("fetchFollowedUsersStories", "Error fetching stories", e)
                            }
                        }
                    } else {
                        Log.d("fetchFollowedUsersStories", "No following users found")
                    }
                } else {
                    Log.d("fetchFollowedUsersStories", "No data exists for following users")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("fetchFollowedUsersStories", "Error fetching following list: ${exception.message}")
            }
    }

    // Helper function to parse stories
    private fun parseStoriesFromResponse(responseData: String): List<Story> {
        Log.d("Response", responseData)
        val storiesList = mutableListOf<Story>()
        try {
            val jsonArray = JSONArray(responseData)

            for (i in 0 until jsonArray.length()) {
                val storyJson = jsonArray.getJSONObject(i)
                val storyId = storyJson.optString("story_id", "")
                val mediaUrl = storyJson.optString("media_url", "")
                val duration = storyJson.optInt("duration", 0)
                val trimmedAudioUrl = storyJson.optString("trimmed_audio_url", null)
                val draggableTextsJsonString = storyJson.optString("draggable_texts", "[]")
                val draggableTexts = try {
                    JSONArray(draggableTextsJsonString)
                } catch (e: JSONException) {
                    Log.e("parseStoriesFromResponse", "Invalid JSON for draggable_texts: $draggableTextsJsonString")
                    JSONArray()
                }

                val createdAt = storyJson.optString("created_at", null)
                val city = storyJson.optString("city", null)
                val country = storyJson.optString("country", null)
                val latitude = storyJson.optDouble("latitude", 0.0)
                val longitude = storyJson.optDouble("longitude", 0.0)
                val userName = storyJson.optString("full_name", "")
                val userProfilePicture = storyJson.optString("profile_picture_url", "")

                if (storyId.isEmpty() || mediaUrl.isEmpty() || createdAt.isNullOrEmpty()) {
                    Log.e("parseStoriesFromResponse", "Invalid story data: $storyJson")
                    continue
                }

                // Convert createdAt string to Date
                val uploadedAt = parseDate(createdAt) ?: continue

                //Story object
                val story = Story(
                    id = storyId,
                    mediaUrl = mediaUrl,
                    duration = duration,
                    trimmedAudioUrl = trimmedAudioUrl,
                    draggableTexts = draggableTexts,
                    uploadedAt = uploadedAt,
                    userName = userName,
                    userProfilePicture = userProfilePicture,
                    city = city,
                    country = country,
                    latitude = latitude,
                    longitude = longitude
                )

                storiesList.add(story)
            }
        } catch (e: JSONException) {
            Log.e("parseStoriesFromResponse", "Error parsing response data", e)
        }
        return storiesList
    }

    private fun updateStoriesUI(stories: List<Story>) {
        highlightsAdapter = UserFeedHighlightsAdapter(stories)
        highlightsRecyclerView.adapter = highlightsAdapter
    }

    fun parseDate(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            dateFormat.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
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

        exploreButton = findViewById(R.id.searchButton)
        exploreButton.setOnClickListener {
            val intent = Intent(this, ExplorePage::class.java)
            startActivity(intent)
        }

        talentExchangeButton=findViewById(R.id.esportsButton)
        talentExchangeButton.setOnClickListener {
            val intent = Intent(this, switchToEsports::class.java)
            intent.putExtra("loadedFromActivity","casual")
            startActivity(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        userFeedAdapter.releaseAllPlayers()
    }

    override fun onPause() {
        super.onPause()
        userFeedAdapter.releaseAllPlayers()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        userFeedAdapter.releaseAllPlayers()
    }
}
