package com.muhaimen.arenax.userProfile

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.gson.Gson
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.ChatActivity
import com.muhaimen.arenax.dataClasses.AnalyticsData
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.dataClasses.Story
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.explore.ExplorePage
import com.muhaimen.arenax.gamesDashboard.otherUserGames
import com.muhaimen.arenax.gamesDashboard.overallLeaderboard
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.uploadStory.viewStory
import com.muhaimen.arenax.userFeed.UserFeed
import com.muhaimen.arenax.userProfile.otherUserEsportsProfile.OtherUsersEsportsProfile
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


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
    private lateinit var talentExchangeButton:ImageView
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
    private lateinit var homeButton: LinearLayout
    private val client = OkHttpClient()
    private lateinit var activity : String
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var receivedUserId: String
    private lateinit var picture:String
    private lateinit var currentUserId: String
    private lateinit var otherUserEsportsProfileButton:LinearLayout
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
        storageReference = FirebaseStorage.getInstance("gs://i210888.appspot.com").reference.child("profileImages/$receivedUserId")
        requestQueue = Volley.newRequestQueue(this)
        activity="otherUserProfile"
        myGamesListRecyclerView = findViewById(R.id.analytics_recyclerview)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        myGamesListAdapter = gamesDashboardAdapter(emptyList(), receivedUserId)
        myGamesListRecyclerView.adapter = myGamesListAdapter
        rankTextView = findViewById(R.id.rankTextView)
        otherUserEsportsProfileButton=findViewById(R.id.esportsProfileButton)

        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        postsRecyclerView = findViewById(R.id.posts_recyclerview)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)

        //fetching user's Data from Servers
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

        otherUserEsportsProfileButton.setOnClickListener {
            val intent = Intent(this, OtherUsersEsportsProfile::class.java).apply {
                putExtra("userId", receivedUserId)
            }
            startActivity(intent)
        }

        myGamesButton= findViewById(R.id.myGamesButton)
        myGamesButton.setOnClickListener {
            val intent = Intent(this, otherUserGames::class.java).apply {
                putExtra("userId", receivedUserId)
            }
            startActivity(intent)
        }

        talentExchangeButton=findViewById(R.id.talentExchangeButton)
        talentExchangeButton.setOnClickListener {
            val intent = Intent(this, switchToEsports::class.java)
            intent.putExtra("loadedFromActivity","casual")
            startActivity(intent)
        }

        profileButton=findViewById(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }
        myGamesListRecyclerView = findViewById(R.id.analytics_recyclerview)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        myGamesListAdapter = gamesDashboardAdapter(emptyList(), receivedUserId)
        myGamesListRecyclerView.adapter = myGamesListAdapter

        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //functionality to send connection request to the user whose profile is visited
        currentUserId= FirebaseManager.getCurrentUserId().toString()
        requestAllianceButton = findViewById(R.id.requestAllianceButton)

        if (currentUserId != null && receivedUserId != null) {
            Log.d("UserProfile", "Current User ID: $currentUserId, Received User ID: $receivedUserId")
            // Check alliance status on activity load
            lifecycleScope.launch(Dispatchers.Main) {
                val allianceStatus = checkIfAlliance(currentUserId, receivedUserId)
                updateButtonState(requestAllianceButton, allianceStatus)
            }
            requestAllianceButton.setOnClickListener {
                handleButtonClick(requestAllianceButton, currentUserId, receivedUserId)
            }
        }

        //navigation bar listeners
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

        homeButton = findViewById(R.id.home)
        homeButton.setOnClickListener {
            val intent = Intent(this, UserFeed::class.java)
            startActivity(intent)
        }

        //reload Activity's data on page reload
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primaryColor)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            fetchUserDetailsFromFirebase()
            fetchUserStories()
            fetchUserPosts()
            fetchUserRank()
            fetchUserGames()
        }
    }

    //handles the connection button state
    private fun updateButtonState(button: Button, allianceStatus: String) {
        when (allianceStatus) {
            "accepted" -> {
                button.setBackgroundColor(resources.getColor(R.color.secondaryColor))
                button.text = "Alliance Established"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = false // Disable button for established alliance
                button.background = resources.getDrawable(R.drawable.searchbar)
            }
            "pending" -> {
                button.setBackgroundColor(resources.getColor(R.color.hinttextColor))
                button.text = "Request Pending"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = true // Disable button for pending alliance
                button.background = resources.getDrawable(R.drawable.searchbar)
            }
            "false" -> {
                button.setBackgroundColor(resources.getColor(R.color.primaryColor))
                button.text = "Request Alliance"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = true // Enable button for new alliance request
                button.background = resources.getDrawable(R.drawable.searchbar)
            }
        }
    }

    // Handles Connection's button click logic
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
                button.background = resources.getDrawable(R.drawable.searchbar)

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
                // Update profile owner's followers node
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
                button.background = resources.getDrawable(R.drawable.searchbar)
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
                .child("following")
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

    //fetch games list of the profile owner
    private fun fetchUserGames() {
        val request = okhttp3.Request.Builder()
            .url("${Constants.SERVER_URL}usergames/user/${receivedUserId}/mygames")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {}
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
                    runOnUiThread {}
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
            val gamesArray = jsonObject.getJSONArray("games")

            myGamesList = List(gamesArray.length()) { index ->
                val gameObject = gamesArray.getJSONObject(index)
                Log.d("MyGamesList", "Parsing game: ${gameObject.getString("gameName")}, Icon URL: ${gameObject.getString("gameIcon")}")
                val graphDataArray = gameObject.getJSONArray("graphData")
                val graphData = List(graphDataArray.length()) { gIndex ->
                    val dataPoint = graphDataArray.getJSONObject(gIndex)
                    val date = dataPoint.getString("date")
                    val totalHours = dataPoint.getDouble("totalHours")
                    Pair(date, totalHours)
                }

                AnalyticsData(
                    gameName = gameObject.getString("gameName"),
                    totalHours = gameObject.getDouble("totalHours"),
                    iconResId = gameObject.getString("gameIcon"),
                    graphData = graphData
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
                    val rank = response.getString("rank")
                    if (rank == "unranked") {
                        rankTextView.text = "Rank: Unranked"
                    } else {
                        val rankInt = rank.toIntOrNull()
                        if (rankInt != null) {
                            rankTextView.text = "Rank: $rankInt"
                        } else {
                            rankTextView.text = "Rank: Unranked"
                        }
                    }
                } catch (e: JSONException) {
                    //addUserToRankingsIfNeeded(receivedUserId)
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching rank: ${error.message}")
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    //function is not required in the current implemented logic
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
        val url = "${Constants.SERVER_URL}stories/user/$receivedUserId/fetchStoryHighlights"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val storiesList = mutableListOf<Story>()
                    for (i in 0 until response.length()) {
                        val storyJson = response.getJSONObject(i)
                        val storyId = storyJson.optString("story_id", "")
                        val mediaUrl = storyJson.optString("media_url", "")
                        val duration = storyJson.optInt("duration", 0)
                        val trimmedAudioUrl = storyJson.optString("trimmed_audio_url", null)
                        val draggableTextsJsonString = storyJson.optString("draggable_texts", "[]") // Handle as string
                        val draggableTexts = try {
                            JSONArray(draggableTextsJsonString)
                        } catch (e: JSONException) {
                            Log.e("fetchUserStories", "Invalid JSON for draggable_texts: $draggableTextsJsonString")
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
                            Log.e("fetchUserStories", "Invalid story data: $storyJson")
                            continue
                        }
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
        val url = "${Constants.SERVER_URL}uploads/user/${receivedUserId}/getUserPosts"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val postsList = mutableListOf<Post>()
                    for (i in 0 until response.length()) {
                        val postJson = response.getJSONObject(i)
                        val postId = postJson.getInt("post_id")
                        val postContent = postJson.optString("post_content", null.toString())
                        val caption = postJson.optString("caption", null.toString())
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
                        val userFullName = postJson.optString("full_name", null.toString())
                        val userProfilePictureUrl = postJson.optString("profile_picture_url", null.toString())

                        // Parse the comments data
                        val commentsDataJson = postJson.optJSONArray("comments")
                        val commentsList = mutableListOf<Comment>()
                        if (commentsDataJson != null) {
                            for (j in 0 until commentsDataJson.length()) {
                                val commentJson = commentsDataJson.getJSONObject(j)
                                val commentId = commentJson.getInt("comment_id")
                                val commentText = commentJson.optString("comment", "")
                                val commentCreatedAt = commentJson.getString("created_at")
                                val commenterName = commentJson.optString("commenter_name", "")
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
                            caption = caption ?: "",
                            sponsored = sponsored,
                            likes = likes,
                            comments = comments,
                            shares = shares,
                            clicks = clicks,
                            city = city,
                            country = country,
                            trimmedAudioUrl = trimmedAudioUrl,
                            createdAt = createdAt,
                            userFullName = userFullName ?: "",
                            userProfilePictureUrl = userProfilePictureUrl ?: "",
                            commentsData = if (commentsList.isNotEmpty()) commentsList else null,
                            isLikedByUser = likedByUser
                        )
                        postsList.add(post)
                    }

                    updatePostsUI(postsList)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, "Error parsing response: ${e.message}")
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching posts: ${error.message}")
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    private fun updatePostsUI(posts: List<Post>) {
        postsAdapter = PostsAdapter(posts)
        postsRecyclerView.adapter = postsAdapter
        postsCount=findViewById(R.id.postsCount)
        postsCount.setText(postsAdapter.itemCount.toString())
    }

    //Refetching Data from Servers on activity resumed
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
                        val storyId = storyJson.optString("story_id", "")
                        val mediaUrl = storyJson.optString("media_url", "")
                        val duration = storyJson.optInt("duration", 0)
                        val trimmedAudioUrl = storyJson.optString("trimmed_audio_url", null)
                        val draggableTextsJsonString = storyJson.optString("draggable_texts", "[]") // Handle as string
                        val draggableTexts = try {
                            JSONArray(draggableTextsJsonString)
                        } catch (e: JSONException) {
                            Log.e("fetchUserStories", "Invalid JSON for draggable_texts: $draggableTextsJsonString")
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
                            Log.e("fetchUserStory", "Invalid story data: $storyJson")
                            continue
                        }
                        val uploadedAt = parseDate(createdAt) ?: continue
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
                    if (storiesList.isNotEmpty()) {
                        // Show the story ring
                        findViewById<ImageView>(R.id.storyRing).visibility = View.VISIBLE
                        // Launch StoryViewActivity if there are stories
                        val gson = Gson()
                        val storiesJson = gson.toJson(storiesList)

                        val intent = Intent(this, viewStory::class.java).apply {
                            putExtra("intentFrom", "UserProfile")
                            putExtra("storiesListJson", storiesJson)
                            putExtra("currentIndex", 0)
                        }
                        startActivity(intent)
                    } else {
                        // Hide the story ring if there are no stories
                        findViewById<ImageView>(R.id.storyRing).visibility = View.GONE
                        navigateToFullProfilePicture()
                    }
                } catch (e: JSONException) {
                    Log.e("fetchUserStory", "JSON parsing error: ${e.message}")
                } catch (e: Exception) {
                    Log.e("fetchUserStory", "Unexpected error: ${e.message}")
                }
            },
            { error -> }
        )

        // Add the request to the request queue
        requestQueue.add(jsonArrayRequest)
    }

    fun parseDate(dateString: String): Date? {
        return try {
            // Updated format to handle the 'Z' (UTC) and milliseconds (SSS)
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC") // Ensure it's parsed as UTC
            format.parse(dateString)
        } catch (e: Exception) {
            Log.e("fetchUserStory", "Date parsing error: ${e.message}")
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
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val hasRecentStory = response.getBoolean("hasRecentStory")
                if (hasRecentStory) {
                    storyRing.visibility = View.VISIBLE
                } else {
                    // No recent stories, keep the ring hidden
                    storyRing.visibility = View.GONE
                }
            },
            { error ->
                error.printStackTrace()
                storyRing.visibility = View.GONE
            }
        )
        Volley.newRequestQueue(storyRing.context).add(jsonObjectRequest)
    }

    private fun fetchUserDataAndStartChat(receiverId: String) {
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

                // Handle failure to retrieve data and start the chat with default values
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", receiverId)
                    putExtra("fullname", "Unknown User")
                    putExtra("gamerTag", "Unknown GamerTag")
                    putExtra("profilePicture", "null")
                    putExtra("gamerRank", "00")
                }
                startActivity(intent)
            }
        }.addOnFailureListener { exception -> }
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