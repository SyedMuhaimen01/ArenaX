package com.muhaimen.arenax.userProfile

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.AuthFailureError
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.ChatActivity
import com.muhaimen.arenax.dataClasses.AnalyticsData
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.dataClasses.Story
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.explore.ExplorePage
import com.muhaimen.arenax.gamesDashboard.MyGamesList
import com.muhaimen.arenax.gamesDashboard.MyGamesListAdapter
import com.muhaimen.arenax.gamesDashboard.otherUserGames
import com.muhaimen.arenax.gamesDashboard.overallLeaderboard
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.uploadStory.viewStory
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import highlightsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class otherUserProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val database = FirebaseDatabase.getInstance()
    private lateinit var storageReference: StorageReference
    private lateinit var myGamesListRecyclerView: RecyclerView
    private lateinit var myGamesListAdapter: gamesDashboardAdapter
    private lateinit var myGamesList: List<AnalyticsData>
    private lateinit var highlightsRecyclerView: RecyclerView
    private lateinit var highlightsAdapter: highlightsAdapter
    private lateinit var exploreButton: ImageView
    private lateinit var postsCount: TextView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var profileImage: ImageView
    private lateinit var bioTextView: TextView
    private lateinit var showMoreTextView: TextView
    private lateinit var requestAllianceButton: Button
    private lateinit var followingTextView: TextView
    private lateinit var followersTextView: TextView
    private lateinit var addPost: ImageView
    private lateinit var storyRing: ImageView
    private lateinit var userData: UserData
    private lateinit var messageButton: Button
    private lateinit var leaderboardButton: ImageView
    private lateinit var profileButton:ImageView
    private lateinit var rankTextView: TextView
    private lateinit var requestQueue: RequestQueue
    private lateinit var myGamesButton: ImageView
    private val client = OkHttpClient()
    private lateinit var activity : String
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var receivedUserId: String
    private lateinit var picture:String
    private lateinit var currentUserId: String
    @SuppressLint("MissingInflatedId", "SetTextI18n", "CutPasteId", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_other_user_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.LogoBackground)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        auth = FirebaseAuth.getInstance()
        receivedUserId = intent.getStringExtra("userId").toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("userData").child(receivedUserId ?: "")
        storageReference = FirebaseStorage.getInstance().reference.child("profileImages/$receivedUserId")
        requestQueue = Volley.newRequestQueue(this)
        activity="otherUserProfile"
        myGamesListRecyclerView = findViewById(R.id.analytics_recyclerview)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        myGamesListAdapter = gamesDashboardAdapter(emptyList(), receivedUserId)
        myGamesListRecyclerView.adapter = myGamesListAdapter

        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        postsRecyclerView = findViewById(R.id.posts_recyclerview)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)

        fetchUserDetailsFromFirebase()
        fetchUserStories()
        fetchUserPosts()
        fetchUserRank()
        fetchUserGames()

        followersTextView = findViewById(R.id.followersTextView)
        followingTextView = findViewById(R.id.followingTextView)

        fetchAndSetCounts(receivedUserId)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        profileImage = findViewById(R.id.profilePicture)
        bioTextView = findViewById(R.id.bioText)
        showMoreTextView = findViewById(R.id.showMore)

        leaderboardButton = findViewById(R.id.leaderboardButton)
        leaderboardButton.setOnClickListener {
            val intent = Intent(this, overallLeaderboard::class.java)
            startActivity(intent)
        }

        myGamesButton= findViewById(R.id.myGamesButton)
        myGamesButton.setOnClickListener {
            val intent = Intent(this, otherUserGames::class.java).apply {
                putExtra("userId", receivedUserId)  // Pass the user ID to the next activity
            }
            startActivity(intent)
        }


        profileButton=findViewById(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }
        // Initialize the RecyclerView for analytics
        myGamesListRecyclerView = findViewById(R.id.analytics_recyclerview)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Set an empty adapter initially
        myGamesListAdapter = gamesDashboardAdapter(emptyList(), receivedUserId)
        myGamesListRecyclerView.adapter = myGamesListAdapter

        // Initialize the RecyclerView for highlights
        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rankTextView = findViewById(R.id.rankTextView)

        currentUserId= FirebaseManager.getCurrentUserId().toString()
        // Assume currentUserId and receivedUserId are already defined.
        requestAllianceButton = findViewById(R.id.requestAllianceButton)

        if (currentUserId != null && receivedUserId != null) {
            // Check alliance status on activity load
            lifecycleScope.launch(Dispatchers.Main) {
                val allianceStatus = checkIfAlliance(currentUserId, receivedUserId)
                updateButtonState(requestAllianceButton, allianceStatus)
            }

            // Set up button click listener
            requestAllianceButton.setOnClickListener {
                handleButtonClick(requestAllianceButton, currentUserId, receivedUserId)
            }
        }

        addPost= findViewById(R.id.addPostButton)
        addPost.setOnClickListener {
            val intent = Intent(this, UploadContent::class.java)
            startActivity(intent)
        }

        exploreButton= findViewById(R.id.exploreButton)
        exploreButton.setOnClickListener {
            val intent = Intent(this, ExplorePage::class.java)
            startActivity(intent)
        }

        messageButton=findViewById(R.id.messageButton)
        messageButton.setOnClickListener {
            Log.d("UserProfile", "Message button clicked")
            fetchUserDataAndStartChat(receivedUserId)
        }
        storyRing = findViewById(R.id.storyRing)
        checkRecentStories(receivedUserId, storyRing)

        profileImage.setOnClickListener {
            onProfilePictureClick()
        }

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primaryColor)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            fetchUserDetailsFromFirebase()
            fetchUserStories()
            fetchUserPosts()
            fetchUserRank()
            fetchUserGames()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "TrackingServiceChannel",
                "Game Tracking Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }


    }


    private fun updateButtonState(button: Button, allianceStatus: String) {
        when (allianceStatus) {
            "accepted" -> {
                button.setBackgroundColor(resources.getColor(R.color.secondaryColor))
                button.text = "Alliance Established"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = false // Disable button for established alliance
            }
            "pending" -> {
                button.setBackgroundColor(resources.getColor(R.color.hinttextColor))
                button.text = "Request Pending"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = true // Disable button for pending alliance
            }
            "false" -> {
                button.setBackgroundColor(resources.getColor(R.color.primaryColor))
                button.text = "Request Alliance"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = true // Enable button for new alliance request
            }
        }
    }

    // Handles button click logic
    @SuppressLint("RestrictedApi")
    private fun handleButtonClick(button: Button, currentUserId: String, receiverId: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("userData")

        when (button.text) {
            "Request Alliance" -> {
                // Update button UI to reflect the pending status
                button.setBackgroundColor(resources.getColor(R.color.hinttextColor))
                button.text = "Request Pending"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = true

                // Create the alliance request object
                val allianceRequest = mapOf(
                    "status" to "pending",
                    "receiverId" to receiverId
                )

                val followerRequest = mapOf(
                    "status" to "pending",
                    "followerId" to currentUserId
                )

                // Update current user's following node
                val currentUserFollowingRef = userRef.child(currentUserId).child("synerG").child("following").child(receiverId)
                // Update receiver's followers node
                val receiverFollowersRef = userRef.child(receiverId).child("synerG").child("followers").child(currentUserId)

                // Perform both updates simultaneously
                val updates = mapOf(
                    currentUserFollowingRef.path.toString() to allianceRequest,
                    receiverFollowersRef.path.toString() to followerRequest
                )

                database.reference.updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("AllianceRequest", "Alliance request sent to $receiverId and added to both nodes")
                        } else {
                            Log.e("AllianceRequest", "Failed to send request: ${task.exception?.message}")
                        }
                    }
            }
            "Request Pending" -> {
                // Update button UI to reflect the request being canceled
                button.setBackgroundColor(resources.getColor(R.color.primaryColor))
                button.text = "Request Alliance"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = true

                // Remove entries from both following and followers nodes
                val currentUserFollowingRef = userRef.child(currentUserId).child("synerG").child("following").child(receiverId)
                val receiverFollowersRef = userRef.child(receiverId).child("synerG").child("followers").child(currentUserId)

                // Perform both deletions simultaneously
                val deletions = mapOf(
                    currentUserFollowingRef.path.toString() to null,
                    receiverFollowersRef.path.toString() to null
                )

                database.reference.updateChildren(deletions)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("AllianceRequest", "Alliance request canceled for $receiverId and removed from both nodes")
                        } else {
                            Log.e("AllianceRequest", "Failed to cancel request: ${task.exception?.message}")
                        }
                    }
            }
        }
    }


    // Checks the alliance status
    suspend fun checkIfAlliance(currentUserId: String, receivedUserId: String): String {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("userData")

        return try {
            val dataSnapshot = userRef.child(currentUserId)
                .child("synerG")
                .child("alliance")
                .child(receivedUserId)
                .get()
                .await()

            if (dataSnapshot.exists()) {
                val status = dataSnapshot.child("status").value?.toString() ?: "false"
                status // Return "accepted", "pending", or unexpected values directly
            } else {
                "false" // No alliance exists
            }
        } catch (e: Exception) {
            Log.e("AllianceCheck", "Failed to check alliance: ${e.message}")
            "false"
        }
    }



    private fun fetchUserGames() {
        val request = okhttp3.Request.Builder()
            .url("${Constants.SERVER_URL}usergames/user/${receivedUserId}/mygames")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@otherUserProfile, "Failed to fetch games", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    if (responseBody.isNotEmpty()) {
                        parseGamesData(responseBody)

                    } else {
                        updateEmptyGameList()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@otherUserProfile, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateEmptyGameList() {
        runOnUiThread {
            myGamesListAdapter.updateGamesList(emptyList())
        }
    }

    private fun parseGamesData(responseBody: String) {
        try {
            val jsonObject = JSONObject(responseBody)
            val gamesArray = jsonObject.getJSONArray("games") // Fetch the 'games' array from the JSON object

            myGamesList = List(gamesArray.length()) { index ->
                val gameObject = gamesArray.getJSONObject(index)
                Log.d("MyGamesList", "Parsing game: ${gameObject.getString("gameName")}, Icon URL: ${gameObject.getString("gameIcon")}")

                // Assuming graphData is coming from the server as a List of objects
                val graphDataArray = gameObject.getJSONArray("graphData")
                val graphData = List(graphDataArray.length()) { gIndex ->
                    val dataPoint = graphDataArray.getJSONObject(gIndex)
                    val date = dataPoint.getString("date") // Get date
                    val totalHours = dataPoint.getDouble("totalHours") // Get total hours
                    Pair(date, totalHours)
                }

                // Create AnalyticsData object
                AnalyticsData(
                    gameName = gameObject.getString("gameName"),
                    totalHours = gameObject.getDouble("totalHours"), // Assuming totalHours is a double
                    iconResId = gameObject.getString("gameIcon"),
                    graphData = graphData // Assign the graph data
                )
            }

            runOnUiThread {
                Log.d("MyGamesList", "Number of games fetched: ${myGamesList.size}")
                myGamesListAdapter.updateGamesList(myGamesList)
            }
        } catch (e: Exception) {
            Log.e("MyGamesList", "Error parsing games data", e)
        }
    }

    private fun fetchUserDetailsFromFirebase() {
        receivedUserId.let { uid ->
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userData = snapshot.getValue(UserData::class.java) ?: UserData()
                        Log.d("UserProfile", "Data loaded from Firebase: $userData")
                        findViewById<TextView>(R.id.userName).text = userData.fullname
                        findViewById<TextView>(R.id.gamerTag).text = userData.gamerTag
                        findViewById<TextView>(R.id.bioText).text = userData.bio
                        val bio=userData.bio
                        swipeRefreshLayout.isRefreshing = false
                        if (bio != null) {
                            if (bio.length > 50) {
                                showMoreTextView.visibility = View.VISIBLE
                            }
                        }
                        showMoreTextView.setOnClickListener {
                            bioTextView.maxLines = Int.MAX_VALUE
                            bioTextView.ellipsize = null
                            showMoreTextView.visibility = View.GONE
                        }
                        picture=userData.profilePicture.toString()
                        userData.profilePicture?.let { url ->
                            Glide.with(this@otherUserProfile)
                                .load(url)
                                .circleCrop()
                                .into(profileImage)
                        }
                    } else {
                        Log.w("UserProfile", "No data found for user ID: $uid")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EditUserProfile", "Database error: ${error.message}")
                }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchUserRank() {
        val checkRankUrl = "${Constants.SERVER_URL}leaderboard/user/${receivedUserId}/rank"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            checkRankUrl,
            null,
            { response ->
                try {
                    val rank = response.getString("rank") // Get rank as a String

                    // Check if the rank is 'unranked' or a number
                    if (rank == "unranked") {
                        rankTextView.text = "Rank: Unranked"
                    } else {
                        val rankInt = rank.toIntOrNull() // Safely convert to Int if it is a valid number
                        if (rankInt != null) {
                            rankTextView.text = "Rank: $rankInt"
                        } else {
                            rankTextView.text = "Rank: Unranked" // Fallback in case of invalid rank format
                        }
                    }
                } catch (e: JSONException) {
                    if (receivedUserId != null) {
                        addUserToRankingsIfNeeded(receivedUserId)
                    }
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching rank: ${error.message}")
                // Toast.makeText(this, "Error fetching rank", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun addUserToRankingsIfNeeded(userId: String) {
        val addRankUrl = "${Constants.SERVER_URL}leaderboard/user/$userId/add"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            addRankUrl,
            null,
            { _ ->
                fetchUserRank()
            },
            { error: VolleyError ->
                Log.e(TAG, "Error adding user to rankings: ${error.message}")
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchUserStories() {
        val userId = auth.currentUser?.uid ?: return
        val url = "${Constants.SERVER_URL}stories/user/$userId/fetchStoryHighlights"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val storiesList = mutableListOf<Story>()
                    for (i in 0 until response.length()) {
                        val storyJson = response.getJSONObject(i)

                        // Safely parse fields with error handling
                        val storyId = storyJson.optString("story_id", "") // Updated to match the backend response
                        val mediaUrl = storyJson.optString("media_url", "")
                        val duration = storyJson.optInt("duration", 0)
                        val trimmedAudioUrl = storyJson.optString("trimmed_audio_url", null)

                        // Handle draggable_texts as a JSON string
                        val draggableTextsJsonString = storyJson.optString("draggable_texts", "[]") // Handle as string
                        val draggableTexts = try {
                            JSONArray(draggableTextsJsonString) // Convert string to JSONArray
                        } catch (e: JSONException) {
                            Log.e("fetchUserStories", "Invalid JSON for draggable_texts: $draggableTextsJsonString")
                            JSONArray() // Return an empty JSONArray in case of error
                        }

                        val createdAt = storyJson.optString("created_at", null)
                        val city = storyJson.optString("city", null)
                        val country = storyJson.optString("country", null)
                        val latitude = storyJson.optDouble("latitude", 0.0)
                        val longitude = storyJson.optDouble("longitude", 0.0)
                        val userName = storyJson.optString("full_name", "")
                        val userProfilePicture = storyJson.optString("profile_picture_url", "")

                        // Ensure mandatory fields are valid
                        if (storyId.isEmpty() || mediaUrl.isEmpty() || createdAt.isNullOrEmpty()) {
                            Log.e("fetchUserStories", "Invalid story data: $storyJson")
                            continue
                        }

                        // Convert createdAt string to Date
                        val uploadedAt = parseDate(createdAt) ?: continue

                        // Create the Story object
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
                    Log.e("Fetecd","Soties:${storiesList}")
                    updateStoriesUI(storiesList)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching stories: ${error.message}")

            }
        )

        requestQueue.add(jsonArrayRequest)
    }


    private fun updateStoriesUI(stories: List<Story>) {
        highlightsAdapter = highlightsAdapter(stories)
        highlightsRecyclerView.adapter = highlightsAdapter
    }

    private fun fetchUserPosts() {
        val userId = auth.currentUser?.uid
        val url = "${Constants.SERVER_URL}uploads/user/${receivedUserId}/getUserPosts"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    // Create a list to hold the user's posts
                    val postsList = mutableListOf<Post>()
                    for (i in 0 until response.length()) {
                        val postJson = response.getJSONObject(i)

                        // Parse fields from the JSON response with safe defaults
                        val postId = postJson.getInt("post_id")
                        val postContent = postJson.optString("post_content", null.toString())
                        val caption = postJson.optString("caption", null.toString()) // Safe string parsing
                        val sponsored = postJson.getBoolean("sponsored")
                        val likes = postJson.getInt("likes")
                        val comments = postJson.getInt("post_comments")
                        val shares = postJson.getInt("shares")
                        val clicks = postJson.getInt("clicks")
                        val city = postJson.optString("city", null.toString())
                        val country = postJson.optString("country", null.toString())
                        val trimmedAudioUrl = postJson.optString("trimmed_audio_url", null.toString())
                        val createdAt = postJson.getString("created_at")
                        val likedByUser = postJson.getBoolean("likedByUser")
                        // Parse the user details safely
                        val userFullName = postJson.optString("full_name", null.toString()) // Ensure it uses the correct field name
                        val userProfilePictureUrl = postJson.optString("profile_picture_url", null.toString())

                        // Parse the comments data
                        val commentsDataJson = postJson.optJSONArray("comments")
                        val commentsList = mutableListOf<Comment>()
                        if (commentsDataJson != null) {
                            for (j in 0 until commentsDataJson.length()) {
                                val commentJson = commentsDataJson.getJSONObject(j)

                                val commentId = commentJson.getInt("comment_id")
                                val commentText = commentJson.optString("comment", "No comment provided") // Safe text parsing
                                val commentCreatedAt = commentJson.getString("created_at")
                                val commenterName = commentJson.optString("commenter_name", "Unknown commenter")
                                val commenterProfilePictureUrl = commentJson.optString("commenter_profile_pic", null.toString())

                                val comment = Comment(
                                    commentId = commentId,
                                    commentText = commentText,
                                    createdAt = commentCreatedAt,
                                    commenterName = commenterName,
                                    commenterProfilePictureUrl = commenterProfilePictureUrl
                                )
                                commentsList.add(comment)
                            }
                        }



                        // Create a Post object
                        val post = Post(
                            postId = postId,
                            postContent = postContent,
                            caption = caption ?: "No caption provided", // Default caption if null
                            sponsored = sponsored,
                            likes = likes,
                            comments = comments,
                            shares = shares,
                            clicks = clicks,
                            city = city,
                            country = country,
                            trimmedAudioUrl = trimmedAudioUrl,
                            createdAt = createdAt,
                            userFullName = userFullName ?: "Unknown user", // Default name if null
                            userProfilePictureUrl = userProfilePictureUrl ?: "path/to/default/profile/picture.jpg", // Default profile picture if null
                            commentsData = if (commentsList.isNotEmpty()) commentsList else null, // Null if no comments
                            isLikedByUser = likedByUser // Add likedByUser attribute
                        )

                        // Add the post to the list
                        postsList.add(post)
                    }

                    // Update the UI with the fetched posts
                    updatePostsUI(postsList)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, "Error parsing response: ${e.message}")
                    // Optionally, notify the user about the error
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching posts: ${error.message}")
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest)
    }

    // Function to update the UI with the fetched posts
    private fun updatePostsUI(posts: List<Post>) {
        postsAdapter = PostsAdapter(posts) // Create a new adapter with the fetched posts
        postsRecyclerView.adapter = postsAdapter
        postsCount=findViewById(R.id.postsCount)
        postsCount.setText(postsAdapter.itemCount.toString())// Set the adapter to RecyclerView
    }


    override fun onResume() {
        super.onResume()
        fetchUserDetailsFromFirebase()
        fetchUserStories()
        fetchUserPosts()
        fetchUserRank()
        fetchUserGames()
    }


    private fun onProfilePictureClick() {
        fetchUserStory(receivedUserId)
    }

    private fun fetchUserStory(userId: String) {
        val url = "${Constants.SERVER_URL}stories/user/$userId/fetchRecentStories"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val storiesList = mutableListOf<Story>()

                    for (i in 0 until response.length()) {
                        val storyJson = response.getJSONObject(i)

                        // Safely parse fields with error handling
                        val storyId = storyJson.optString("story_id", "")
                        val mediaUrl = storyJson.optString("media_url", "")
                        val duration = storyJson.optInt("duration", 0)
                        val trimmedAudioUrl = storyJson.optString("trimmed_audio_url", null)

                        // Handle draggable_texts as a JSON string
                        val draggableTextsJsonString = storyJson.optString("draggable_texts", "[]") // Handle as string
                        val draggableTexts = try {
                            JSONArray(draggableTextsJsonString) // Convert string to JSONArray
                        } catch (e: JSONException) {
                            Log.e("fetchUserStory", "Invalid JSON for draggable_texts: $draggableTextsJsonString")
                            JSONArray() // Return an empty JSONArray in case of error
                        }

                        val createdAt = storyJson.optString("created_at", null)
                        val city = storyJson.optString("city", null)
                        val country = storyJson.optString("country", null)
                        val latitude = storyJson.optDouble("latitude", 0.0)
                        val longitude = storyJson.optDouble("longitude", 0.0)
                        val userName = storyJson.optString("full_name", "")
                        val userProfilePicture = storyJson.optString("profile_picture_url", "")

                        // Ensure mandatory fields are valid
                        if (storyId.isEmpty() || mediaUrl.isEmpty() || createdAt.isNullOrEmpty()) {
                            Log.e("fetchUserStory", "Invalid story data: $storyJson")
                            continue
                        }

                        // Convert createdAt string to Date
                        val uploadedAt = parseDate(createdAt) ?: continue

                        // Create the Story object
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

                    // Handle story presence
                    val storyRing = findViewById<ImageView>(R.id.storyRing)
                    if (storiesList.isNotEmpty()) {
                        storyRing.visibility = View.VISIBLE

                        // Launch StoryViewActivity if stories are available
                        val intent = Intent(this, viewStory::class.java).apply {
                            putParcelableArrayListExtra("storiesList", ArrayList(storiesList))
                            putExtra("currentIndex", 0)
                        }
                        startActivity(intent)
                    } else {
                        // Hide the story ring and navigate to the profile picture
                        storyRing.visibility = View.GONE
                        navigateToFullProfilePicture()
                    }
                } catch (e: JSONException) {
                    Log.e("fetchUserStory", "JSON parsing error: ${e.message}")
                } catch (e: Exception) {
                    Log.e("fetchUserStory", "Unexpected error: ${e.message}")
                }
            },
            { error ->
                val errorMessage = when (error) {
                    is TimeoutError -> "Request timed out"
                    is NoConnectionError -> "No internet connection"
                    is AuthFailureError -> "Authentication error: ${error.message}"
                    is ServerError -> "Server error: ${String(error.networkResponse?.data ?: ByteArray(0))}"
                    is NetworkError -> "Network error: ${error.message}"
                    else -> "Response parse error: ${error.message}"
                }
                Log.e("fetchUserStory", errorMessage)
                Toast.makeText(this, "Failed to fetch stories: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the request queue
        requestQueue.add(jsonArrayRequest)
    }

    // Helper function to parse the date string
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



    private fun navigateToFullProfilePicture() {

        val intent = Intent(this, ProfilePictureActivity::class.java).apply {
            putExtra("profilePictureUrl", picture)
        }
        startActivity(intent)
    }

    fun checkRecentStories(userId: String, storyRing: ImageView) {
        val url = "${Constants.SERVER_URL}stories/user/$userId/hasRecentStory"

        // Create a JsonObjectRequest to make the network call
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                // Assuming the response is a JSON object containing a boolean field 'hasRecentStory'
                val hasRecentStory = response.getBoolean("hasRecentStory")
                if (hasRecentStory) {
                    // User has recent stories, make the ring visible
                    storyRing.visibility = View.VISIBLE
                } else {
                    // No recent stories, keep the ring hidden
                    storyRing.visibility = View.GONE
                }
            },
            { error ->
                // Handle error
                error.printStackTrace()
                // Hide ring on error as well
                storyRing.visibility = View.GONE
            }
        )

        // Add the request to the RequestQueue
        Volley.newRequestQueue(storyRing.context).add(jsonObjectRequest)
    }

    private fun fetchUserDataAndStartChat(receiverId: String) {
        // Log the receiverId to see what data is being passed
        Log.d("ChatActivity", "fetchUserDataAndStartChat called with receiverId: $receiverId")

        val database = FirebaseManager.getDatabseInstance()
        val userRef = database.getReference("userData").child(receiverId)

        // Try to fetch the user data from Firebase
        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                // Fetch only the necessary user data
                val profileImageUrl = dataSnapshot.child("profilePicture").value?.toString() ?: ""
                val fullname = dataSnapshot.child("fullname").value?.toString() ?: "Unknown User"
                val gamerTag = dataSnapshot.child("gamerTag").value?.toString() ?: "Unknown GamerTag"
                val gamerRank = dataSnapshot.child("gamerRank").value?.toString() ?: "00" // Adjust logic to fetch gamerRank if needed

                // Log the fetched data for debugging
                Log.d("ChatActivity", "Data retrieved for $receiverId: Fullname = $fullname, GamerTag = $gamerTag, ProfilePicture = $profileImageUrl, GamerRank = $gamerRank")

                // Create intent and pass user data
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", receiverId)
                    putExtra("fullname", fullname)
                    putExtra("gamerTag", gamerTag)
                    putExtra("profilePicture", profileImageUrl)
                    putExtra("gamerRank", gamerRank)
                }
                // Start the ChatActivity with the data
                startActivity(intent)
            } else {
                // If data doesn't exist in Firebase, log the failure
                Log.d("ChatActivity", "No data found for receiverId: $receiverId. Defaulting to Unknown User data.")

                // Handle failure to retrieve data and start the chat with default values
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", receiverId)
                    putExtra("fullname", "Unknown User")
                    putExtra("gamerTag", "Unknown GamerTag")
                    putExtra("profilePicture", "null") // or a placeholder image URL
                    putExtra("gamerRank", "00") // Default value
                }
                startActivity(intent)
            }
        }.addOnFailureListener { exception ->
            // Log failure with exception message
            Log.e("ChatActivity", "Error retrieving user data for $receiverId: ${exception.message}")

            // In case of failure, handle by passing default values
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("userId", receiverId)
                putExtra("fullname", "Unknown User")
                putExtra("gamerTag", "Unknown GamerTag")
                putExtra("profilePicture", "null") // or a placeholder image URL
                putExtra("gamerRank", "00") // Default value
            }
            startActivity(intent)
        }
    }
    private fun fetchAndSetCounts(userId: String) {
        val followersRef = database.getReference("userData/$userId/synerG/followers")
        val followingRef = database.getReference("userData/$userId/synerG/following")

        // Fetch and count followers with status "accepted"
        followersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val acceptedFollowersCount = snapshot.children.count {
                    it.child("status").value?.toString() == "accepted"
                }
                followersTextView.text = acceptedFollowersCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Error fetching followers: ${error.message}")
            }
        })

        // Fetch and count following with status "accepted"
        followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val acceptedFollowingCount = snapshot.children.count {
                    it.child("status").value?.toString() == "accepted"
                }
                followingTextView.text = acceptedFollowingCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Error fetching following: ${error.message}")
            }
        })
    }
}