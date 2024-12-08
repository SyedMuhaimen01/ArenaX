package com.muhaimen.arenax.userProfile

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import com.muhaimen.arenax.Threads.ChatService
import com.muhaimen.arenax.accountSettings.accountSettings
import com.muhaimen.arenax.dataClasses.AnalyticsData
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.dataClasses.Story
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.editProfile.editProfile
import com.muhaimen.arenax.gamesDashboard.MyGamesList

import com.muhaimen.arenax.explore.ExplorePage

import com.muhaimen.arenax.gamesDashboard.overallLeaderboard
import com.muhaimen.arenax.screenTime.ScreenTimeService
import com.muhaimen.arenax.synergy.synergy
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.uploadStory.uploadStory

import com.muhaimen.arenax.uploadStory.viewStory

import com.muhaimen.arenax.userFeed.UserFeed

import com.muhaimen.arenax.utils.Constants
import highlightsAdapter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.parser.ParseError
import java.io.IOException
import java.text.ParseException
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class UserProfile : AppCompatActivity() {
    private val USAGE_STATS_PERMISSION_REQUEST_CODE = 1001
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val database = FirebaseDatabase.getInstance()
    private lateinit var storageReference: StorageReference
    private lateinit var myGamesListRecyclerView: RecyclerView
    private lateinit var myGamesListAdapter: gamesDashboardAdapter
    private lateinit var myGamesList: List<AnalyticsData>
    private lateinit var highlightsRecyclerView: RecyclerView
    private lateinit var highlightsAdapter: highlightsAdapter
    private lateinit var exploreButton:ImageView
    private lateinit var postsCount:TextView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var profileImage: ImageView
    private lateinit var bioTextView: TextView
    private lateinit var showMoreTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var myGamesButton: ImageView
    private lateinit var addPost: ImageView
    private lateinit var uploadStoryButton: ImageView
    private lateinit var homeButton: LinearLayout
    private lateinit var storyRing: ImageView
    private lateinit var userData: UserData
    private lateinit var settingsButton:Button
    private lateinit var leaderboardButton: ImageView
    private lateinit var rankTextView: TextView
    private lateinit var followersLinearLayout:LinearLayout
    private lateinit var followingLinearLayout:LinearLayout
    private lateinit var followingTextView: TextView
    private lateinit var followersTextView: TextView
    private lateinit var requestQueue: RequestQueue
    private val client = OkHttpClient()
    private lateinit var activity : String
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var userId:String
    private val sharedPreferences by lazy { getSharedPreferences("MyGamesPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences2 by lazy { getSharedPreferences("MyStoriesPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences3 by lazy { getSharedPreferences("MyPostsPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences4 by lazy { getSharedPreferences("MyRankPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences5 by lazy { getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences6 by lazy { getSharedPreferences("UserLocationPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences7 by lazy {getSharedPreferences("userInterests", Context.MODE_PRIVATE)}

    @SuppressLint("MissingInflatedId", "SetTextI18n", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.LogoBackground)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        databaseReference = FirebaseDatabase.getInstance().getReference("userData").child(auth.currentUser?.uid ?: "")
        storageReference = FirebaseStorage.getInstance().reference.child("profileImages/${auth.currentUser?.uid}")
        requestQueue = Volley.newRequestQueue(this)
        activity="UserProfile"
        myGamesListRecyclerView = findViewById(R.id.analytics_recyclerview)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        myGamesListAdapter = gamesDashboardAdapter(emptyList(), userId)
        myGamesListRecyclerView.adapter = myGamesListAdapter
        followersLinearLayout=findViewById(R.id.followersLinearLayout)
        followingLinearLayout=findViewById(R.id.followingLinearLayout)


        postsRecyclerView = findViewById(R.id.posts_recyclerview)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)

        if (!checkUsageStatsPermission()) {
            requestUsageStatsPermission()
        } else {
            startTrackingService()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            getUserLocation()
        } else {
            showLocationPermissionDialog()
        }

        if (!isConnected()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            loadUserDataFromSharedPreferences()
            loadStoriesFromSharedPreferences()
            loadPostsFromSharedPreferences()
            loadRankFromPreferences()
            loadGamesFromPreferences()
        } else {
            fetchUserDetailsFromFirebase()
            fetchUserStories()
            fetchUserPosts()
            fetchUserRank()
            fetchUserGames()
            if(!getInterestsGenerated(userId))
            {
                GenerateUserInterests()
                setInterestsGenerated(userId,true)
            }
        }
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        profileImage = findViewById(R.id.profilePicture)
        bioTextView = findViewById(R.id.bioText)
        showMoreTextView = findViewById(R.id.showMore)

        leaderboardButton = findViewById(R.id.leaderboardButton)
        leaderboardButton.setOnClickListener {
            val intent = Intent(this, overallLeaderboard::class.java)
            startActivity(intent)
        }

        followersLinearLayout.setOnClickListener {
            val intent = Intent(this, synergy::class.java)
            startActivity(intent)
        }

        followersTextView=findViewById(R.id.followersTextView)
        followingTextView=findViewById(R.id.followingTextView)
        if (auth.currentUser != null) {
            fetchAndSetCounts(auth.currentUser?.uid ?: "")
        } else {
            Log.e("ProfileActivity", "Current user ID is null.")
        }



        val intent2 = Intent(this, ChatService::class.java)
        startService(intent2)

        followingLinearLayout.setOnClickListener {
            val intent = Intent(this, synergy::class.java)
            startActivity(intent)
        }
        // Initialize the RecyclerView for analytics
        myGamesListRecyclerView = findViewById(R.id.analytics_recyclerview)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Set an empty adapter initially
        myGamesListAdapter = gamesDashboardAdapter(emptyList(),userId)
        myGamesListRecyclerView.adapter = myGamesListAdapter


        // Initialize the RecyclerView for highlights
        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rankTextView = findViewById(R.id.rankTextView)


        uploadStoryButton = findViewById(R.id.uploadStoryButton)
        uploadStoryButton.setOnClickListener {
            val intent = Intent(this, uploadStory::class.java)
            startActivity(intent)
        }


        homeButton = findViewById(R.id.home)
        homeButton.setOnClickListener {
            val intent = Intent(this, UserFeed::class.java)
            startActivity(intent)
        }

        editProfileButton= findViewById(R.id.editProfileButton)
        editProfileButton.setOnClickListener {
            val intent = Intent(this, editProfile::class.java)
            startActivity(intent)
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

        settingsButton=findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, accountSettings::class.java)
            startActivity(intent)
        }
        myGamesButton= findViewById(R.id.myGamesButton)
        myGamesButton.setOnClickListener {
            val intent = Intent(this, MyGamesList::class.java)
            startActivity(intent)
        }

        storyRing = findViewById(R.id.storyRing)
        val userId=auth.currentUser?.uid
        if (userId != null) {
            checkRecentStories(userId, storyRing)
        }

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
            fetchAndSetCounts(auth.currentUser?.uid ?: "")

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

    private fun loadGamesFromPreferences() {
        val jsonString = sharedPreferences.getString("gamesList", null)
        if (jsonString != null) {
            parseGamesData(jsonString)
        } else {
            fetchUserGames()
        }
    }

    private fun fetchUserGames() {
        val request = okhttp3.Request.Builder()
            .url("${Constants.SERVER_URL}usergames/user/${auth.currentUser?.uid}/mygames")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    //Toast.makeText(this@UserProfile, "Failed to fetch games", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    if (responseBody.isNotEmpty()) {
                        parseGamesData(responseBody)
                        saveGamesToPreferences(responseBody)
                    } else {
                        updateEmptyGameList()
                    }
                } else {
                    runOnUiThread {
                        //Toast.makeText(this@UserProfile, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateEmptyGameList() {
        with(sharedPreferences.edit()) {
            putString("gamesList", "[]")
            apply()
        }
        runOnUiThread {
            myGamesListAdapter.updateGamesList(emptyList())
            //Toast.makeText(this@UserProfile, "No games found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveGamesToPreferences(gamesJson: String) {
        with(sharedPreferences.edit()) {
            putString("gamesList", gamesJson)
            apply()
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


    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun fetchUserDetailsFromFirebase() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userData = snapshot.getValue(UserData::class.java) ?: UserData()
                        Log.d("UserProfile", "Data loaded from Firebase: $userData")
                        findViewById<TextView>(R.id.userName).setText(userData.fullname)
                        findViewById<TextView>(R.id.gamerTag).setText(userData.gamerTag)
                        findViewById<TextView>(R.id.bioText).setText(userData.bio)
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

                        userData.profilePicture?.let { url ->
                            Glide.with(this@UserProfile)
                                .load(url)
                                .circleCrop()
                                .into(profileImage)
                        }
                        saveUserDataToSharedPreferences(userData)
                    } else {
                        Log.w("UserProfile", "No data found for user ID: $uid")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EditUserProfile", "Database error: ${error.message}")
                    loadUserDataFromSharedPreferences()
                }
            })
        }
    }

    // Check if the app has usage stats permission
    private fun checkUsageStatsPermission(): Boolean {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Request the user to grant usage stats permission
    private fun requestUsageStatsPermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun startTrackingService() {
        // Start your tracking service
        val intent = Intent(this, ScreenTimeService::class.java)
        startService(intent)
    }


    @SuppressLint("SetTextI18n")
    private fun fetchUserRank() {
        val userId = auth.currentUser?.uid
        val checkRankUrl = "${Constants.SERVER_URL}leaderboard/user/${userId}/rank"
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

                    saveRankToPreferences(rank)

                } catch (e: JSONException) {
                    if (userId != null) {
                        addUserToRankingsIfNeeded(userId)
                    }
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching rank: ${error.message}")
                // Toast.makeText(this, "Error fetching rank", Toast.LENGTH_SHORT).show()
                loadRankFromPreferences()
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
            //    Toast.makeText(this, "Error adding user to rankings", Toast.LENGTH_SHORT).show()
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
                    saveStoriesDataToSharedPreference(storiesList)
                    updateStoriesUI(storiesList)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching stories: ${error.message}")
                loadStoriesFromSharedPreferences() // Optionally load stories from SharedPreferences if error occurs
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
        val url = "${Constants.SERVER_URL}uploads/user/$userId/getUserPosts"

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

                    // Save posts to shared preferences for offline use
                    savePostsDataToSharedPreference(postsList)

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
                // Load cached posts if the request fails
                loadPostsFromSharedPreferences()
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest)
    }


    // Function to update the UI with the fetched posts
    @SuppressLint("SetTextI18n")
    private fun updatePostsUI(posts: List<Post>) {
        postsAdapter = PostsAdapter(posts) // Create a new adapter with the fetched posts
        postsRecyclerView.adapter = postsAdapter
        postsCount=findViewById(R.id.postsCount)
        postsCount.setText(postsAdapter.itemCount.toString())// Set the adapter to RecyclerView
        val adapter = postsAdapter
        val totalHeight = adapter.itemCount.let { count ->
            // Calculate the height dynamically based on the number of items and individual item height
            val itemHeight = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._120sdp) // Or calculate dynamically

            if (count>3)
            {
                (itemHeight * count)/3
            }
            else{
                itemHeight * count
            }
        }

        postsRecyclerView.layoutParams.height = totalHeight
        postsRecyclerView.requestLayout()
    }

    private fun saveRankToPreferences(rank: String) {
        with(sharedPreferences4.edit()) {
            putString("userRank", rank)  // Store rank as a String
            apply()
        }
    }

    @SuppressLint("SetTextI18n")
    fun loadRankFromPreferences() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val rank = sharedPreferences.getInt("rank_key", -1) // Assuming rank is stored as an integer
        if (rank == -1) {
            // Handle case where rank is not set (use "Unranked" or similar logic)
            rankTextView.text = "Unranked"
        } else {
            // Handle the case where rank is a valid integer
            rankTextView.text = rank.toString()
        }
    }


    private fun loadUserDataFromSharedPreferences() {
        val userId = sharedPreferences5.getString("userId", "")
        val fullname = sharedPreferences5.getString("fullname", "")
        val gamerTag = sharedPreferences5.getString("gamerTag", "")
        val bio = sharedPreferences5.getString("bio", "")
        val profilePicture = sharedPreferences5.getString("profilePicture", "")

        findViewById<TextView>(R.id.userName).text = fullname
        findViewById<TextView>(R.id.gamerTag).text = gamerTag
        findViewById<TextView>(R.id.bioText).text = bio

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

        profilePicture?.let { url ->
            Glide.with(this@UserProfile)
                .load(url)
                .circleCrop()
                .into(profileImage)
        }
    }

    private fun saveUserDataToSharedPreferences(userData: UserData) {
        with(sharedPreferences5.edit()) {
            putString("userId", userData.userId)
            putString("fullname", userData.fullname)
            putString("gamerTag", userData.gamerTag)
            putString("bio", userData.bio)
            putString("profilePicture", userData.profilePicture)
            apply()
        }
    }

    private fun saveStoriesDataToSharedPreference(stories: List<Story>) {
        val storiesJsonArray = JSONArray().apply {
            stories.forEach { story ->
                put(JSONObject().apply {
                    put("id", story.id)
                    put("media_url", story.mediaUrl)
                    put("duration", story.duration)
                    put("trimmed_audio_url", story.trimmedAudioUrl)
                    put("draggable_texts", story.draggableTexts)
                    put("created_at", story.uploadedAt?.time ?: 0L) // Store as timestamp
                    put("full_name", story.userName) // Store user name
                    put("profile_picture_url", story.userProfilePicture) // Store user profile picture URL
                    put("city", story.city) // Store user's city
                    put("country", story.country) // Store user's country
                    put("latitude", story.latitude) // Store latitude
                    put("longitude", story.longitude) // Store longitude
                })
            }
        }.toString()

        with(sharedPreferences2.edit()) {
            putString("storiesList", storiesJsonArray.toString())
            apply()
        }
    }

    private fun loadStoriesFromSharedPreferences() {
        val storiesJson = sharedPreferences2.getString("storiesList", null)
        if (storiesJson != null) {
            val stories = mutableListOf<Story>()
            val jsonArray = JSONArray(storiesJson)
            for (i in 0 until jsonArray.length()) {
                val storyJson = jsonArray.getJSONObject(i)
                val uploadedAt = storyJson.getLong("created_at") // Get the timestamp
                val story = Story(
                    storyJson.getString("id"),
                    storyJson.getString("media_url"),
                    storyJson.getInt("duration"),
                    storyJson.optString("trimmed_audio_url", null),
                    storyJson.optJSONArray("draggable_texts"),
                    Date(uploadedAt),
                    storyJson.getString("full_name"), // Get user name
                    storyJson.getString("profile_picture_url"), // Get user profile picture URL
                    storyJson.optString("city", null), // Get user's city
                    storyJson.optString("country", null), // Get user's country
                    storyJson.optDouble("latitude", Double.NaN).takeIf { it != Double.NaN }, // Get latitude
                    storyJson.optDouble("longitude", Double.NaN).takeIf { it != Double.NaN } // Get longitude
                )
                stories.add(story)
            }
            updateStoriesUI(stories)
        }
    }




    private fun savePostsDataToSharedPreference(posts: List<Post>) {
        val postsJsonArray = JSONArray().apply {
            posts.forEach { post ->
                put(JSONObject().apply {
                    put("post_id", post.postId)
                    put("post_content", post.postContent ?: JSONObject.NULL)  // Handle null content
                    put("caption", post.caption ?: JSONObject.NULL)          // Handle null caption
                    put("sponsored", post.sponsored)
                    put("likes", post.likes)
                    put("post_comments", post.comments)
                    put("shares", post.shares)
                    put("clicks", post.clicks)
                    put("city", post.city ?: JSONObject.NULL)                // Handle null city
                    put("country", post.country ?: JSONObject.NULL)          // Handle null country
                    put("trimmed_audio_url", post.trimmedAudioUrl ?: JSONObject.NULL) // Handle null audio URL
                    put("created_at", post.createdAt)

                    // Add user info
                    put("userFullName", post.userFullName ?: JSONObject.NULL) // Handle null user full name
                    put("userProfilePictureUrl", post.userProfilePictureUrl ?: JSONObject.NULL) // Handle null profile picture

                    // Add comments data
                    val commentsJsonArray = JSONArray()
                    post.commentsData?.forEach { comment ->
                        commentsJsonArray.put(JSONObject().apply {
                            put("comment_id", comment.commentId)
                            put("comment_text", comment.commentText)
                            put("created_at", comment.createdAt)
                            put("commenter_name", comment.commenterName)
                            put("commenter_profile_picture_url", comment.commenterProfilePictureUrl ?: JSONObject.NULL)
                        })
                    }
                    put("commentsData", commentsJsonArray)

                    put("likedByUser", post.isLikedByUser)
                })
            }
        }.toString()

        with(sharedPreferences3.edit()) {
            putString("postsList", postsJsonArray.toString())
            apply()
        }
    }



    private fun saveLocationToSharedPreferences(city: String, country: String, latitude: Double, longitude: Double) {
        with(sharedPreferences6.edit()) {
            putString("city", city)
            putString("country", country)
            putFloat("latitude", latitude.toFloat())   // Store latitude as Float
            putFloat("longitude", longitude.toFloat()) // Store longitude as Float
            apply()
        }
    }


    private fun loadLocationFromSharedPreferences(): Triple<String?, String?, Pair<Double, Double>?> {
        val city = sharedPreferences6.getString("city", null)
        val country = sharedPreferences6.getString("country", null)

        // Retrieve latitude and longitude as Floats, and then convert them back to Doubles
        val latitude = sharedPreferences6.getFloat("latitude", 0f).toDouble()
        val longitude = sharedPreferences6.getFloat("longitude", 0f).toDouble()

        // Return the data in a Triple: City, Country, and the Pair of latitude and longitude
        return Triple(city, country, Pair(latitude, longitude))
    }


    private fun loadPostsFromSharedPreferences() {
        val postsJson = sharedPreferences3.getString("postsList", null)
        if (postsJson != null) {
            val posts = mutableListOf<Post>()
            val jsonArray = JSONArray(postsJson)

            for (i in 0 until jsonArray.length()) {
                val postJson = jsonArray.getJSONObject(i)

                // Parse post details
                val post = (if (postJson.isNull("userFullName")) null else postJson.getString("userFullName"))?.let {
                    Post(
                        postId = postJson.getInt("post_id"),
                        postContent = if (postJson.isNull("post_content")) null else postJson.getString("post_content"),
                        caption = if (postJson.isNull("caption")) null else postJson.getString("caption"),
                        sponsored = postJson.getBoolean("sponsored"),
                        likes = postJson.getInt("likes"),
                        comments = postJson.getInt("post_comments"),
                        shares = postJson.getInt("shares"),
                        clicks = postJson.getInt("clicks"),
                        city = if (postJson.isNull("city")) null else postJson.getString("city"),
                        country = if (postJson.isNull("country")) null else postJson.getString("country"),
                        trimmedAudioUrl = if (postJson.isNull("trimmed_audio_url")) null else postJson.getString("trimmed_audio_url"),
                        createdAt = postJson.getString("created_at"),

                        // Parse user information
                        userFullName = it,
                        userProfilePictureUrl = if (postJson.isNull("userProfilePictureUrl")) null else postJson.getString("userProfilePictureUrl"),

                        // Parse comments data
                        commentsData = mutableListOf<Comment>().apply {
                            val commentsJsonArray = postJson.getJSONArray("commentsData")
                            for (j in 0 until commentsJsonArray.length()) {
                                val commentJson = commentsJsonArray.getJSONObject(j)
                                val comment = Comment(
                                    commentId = commentJson.getInt("comment_id"),
                                    commentText = commentJson.getString("comment_text"),
                                    createdAt = commentJson.getString("created_at"),
                                    commenterName = commentJson.getString("commenter_name"),
                                    commenterProfilePictureUrl = if (commentJson.isNull("commenter_profile_picture_url")) null else commentJson.getString("commenter_profile_picture_url")
                                )
                                add(comment)
                            }
                        },

                        // Parse isLikedByUser field
                        isLikedByUser = postJson.getBoolean("likedByUser") // Adding the likedByUser field
                    )
                }
                if (post != null) {
                    posts.add(post)
                }
            }
            updatePostsUI(posts)
        }
    }



    override fun onResume() {
        super.onResume()
        if(activity=="EditProfile")
        {
            fetchUserDetailsFromFirebase()
        } else{
            loadUserDataFromSharedPreferences()
        }
        val filter = IntentFilter("NEW_POST_ADDED")
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)
        val filter2 = IntentFilter("NEW_STORY_ADDED")
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver2, filter2)
        loadGamesFromPreferences()
        loadRankFromPreferences()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            fetchUserPosts()
        }
    }
    private val broadcastReceiver2 = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            fetchUserStories()
            checkRecentStories(auth.currentUser?.uid ?: "", storyRing)
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver2)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showLocationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("For improved user experience, this app requires user's location. Please allow the app to discover user's location.")
            .setPositiveButton("Allow") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude

                    getCityAndCountry(latitude, longitude)
                } ?: run {
                   // Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
             //   Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCityAndCountry(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val city = addresses[0].locality
                    val country = addresses[0].countryName
                    val (previousCity, previousCountry) = loadLocationFromSharedPreferences()

                    // Check if the city or country has changed
                    if (city != previousCity || country != previousCountry) {
                        // Now also send the latitude and longitude to updateUserLocation function
                        updateUserLocation(city, country, latitude, longitude)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting city and country: ${e.message}")
        }
    }


    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserLocation(city: String?, country: String?, latitude: Double?, longitude: Double?) {

        val url="${Constants.SERVER_URL}updateLocation/user/$userId/userLocation"

        // Create the request body with city, country, latitude, and longitude
        val requestBody = JSONObject().apply {
            put("city", city)
            put("country", country)
            put("latitude", latitude)
            put("longitude", longitude)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            { _ ->
                // Save the updated location (city, country, latitude, longitude) to SharedPreferences
                saveLocationToSharedPreferences(city ?: "", country ?: "", latitude ?: 0.0, longitude ?: 0.0)
            },
            { error: VolleyError ->
                Log.e(TAG, "Error updating user location: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }



    private fun onProfilePictureClick() {
        val userId = auth.currentUser?.uid ?: return
        fetchUserStory(userId)
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







    private fun calculateHoursAgo(createdAt: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val createdDate = dateFormat.parse(createdAt)

        // Get the current time
        val currentTime = System.currentTimeMillis()

        // Calculate the difference in milliseconds
        val differenceInMillis = currentTime - createdDate.time

        // Convert milliseconds to hours
        return TimeUnit.MILLISECONDS.toHours(differenceInMillis)
    }


    private fun navigateToFullProfilePicture() {
        val profilePictureUrl = sharedPreferences5.getString("profilePicture", null)
        val intent = Intent(this, ProfilePictureActivity::class.java).apply {
            putExtra("profilePictureUrl", profilePictureUrl)
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

    private fun fetchAndSetCounts(userId: String) {
        val followersRef = database.getReference("userData/$userId/synerG/followers")
        val followingRef = database.getReference("userData/$userId/synerG/following")

        // Fetch and count followers with status "accepted"
        followersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
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
            @SuppressLint("SetTextI18n")
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

    fun GenerateUserInterests() {
        // URL for the API endpoint (replace with actual URL)
        val url = "${Constants.SERVER_URL}userIntertests/user/$userId/generateInterests"

        // Create a StringRequest to call the API
        val stringRequest = StringRequest(
            Request.Method.POST, url,
            { response ->
                // Handle the response
                Log.d("VolleyResponse", "Response: $response")
                // You can handle the response here
            },
            { error ->
                // Handle error
                Log.e("VolleyError", "Error: ${error.message}")
            }
        )


        requestQueue.add(stringRequest)
    }

    fun setInterestsGenerated( userId: String, value: Boolean) {
        val editor = sharedPreferences7.edit()
        editor.putBoolean("interestsGenerated_$userId", value)
        editor.apply()  // Commit the change asynchronously
    }

    fun getInterestsGenerated( userId: String): Boolean {
        return sharedPreferences7.getBoolean("interestsGenerated_$userId", false)
    }
}


