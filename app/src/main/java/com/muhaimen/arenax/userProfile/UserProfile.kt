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
import android.widget.ImageButton
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
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
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
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.ChatService
import com.muhaimen.arenax.accountSettings.accountSettings
import com.muhaimen.arenax.dataClasses.AnalyticsData
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.dataClasses.Story
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.editProfile.editProfile
import com.muhaimen.arenax.gamesDashboard.MyGamesList

import com.muhaimen.arenax.explore.ExplorePage

import com.muhaimen.arenax.gamesDashboard.MyGamesListAdapter
import com.muhaimen.arenax.gamesDashboard.overallLeaderboard
import com.muhaimen.arenax.notifications.Notifications
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class UserProfile : AppCompatActivity() {
    private val USAGE_STATS_PERMISSION_REQUEST_CODE = 1001
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val database = FirebaseDatabase.getInstance()
    private lateinit var storageReference: StorageReference
    private lateinit var myGamesListRecyclerView: RecyclerView
    private lateinit var myGamesListAdapter: MyGamesListAdapter
    private lateinit var myGamesList: List<AnalyticsData>
    private lateinit var highlightsRecyclerView: RecyclerView
    private lateinit var highlightsAdapter: highlightsAdapter
    private lateinit var exploreButton:ImageButton
    private lateinit var postsCount:TextView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var profileImage: ImageView
    private lateinit var bioTextView: TextView
    private lateinit var showMoreTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var myGamesButton: ImageButton
    private lateinit var addPost: ImageButton
    private lateinit var uploadStoryButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var storyRing: ImageView
    private lateinit var userData: UserData
    private lateinit var settingsButton:Button
    private lateinit var leaderboardButton: ImageButton
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
        myGamesListAdapter = MyGamesListAdapter(emptyList(), userId)
        myGamesListRecyclerView.adapter = myGamesListAdapter
        followersLinearLayout=findViewById(R.id.followersLinearLayout)
        followingLinearLayout=findViewById(R.id.followingLinearLayout)

        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

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
            //if(!getInterestsGenerated(userId))
           // {
                GenerateUserInterests()
               // setInterestsGenerated(userId,true)
           // }
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
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Set an empty adapter initially
        myGamesListAdapter = MyGamesListAdapter(emptyList(),userId)
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
                    val rank = response.getInt("rank")

                    if(rank===1)
                    {
                        rankTextView.text = "Rank: Unranked"
                    }else{
                        rankTextView.text = "Rank: $rank"
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
          //      Toast.makeText(this, "Error fetching rank", Toast.LENGTH_SHORT).show()
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
                        val storyId = storyJson.getInt("id")
                        val mediaUrl = storyJson.getString("media_url")
                        val duration = storyJson.getInt("duration")
                        val trimmedAudioUrl = storyJson.optString("trimmed_audio_url", null)
                        val draggableTexts = storyJson.optJSONArray("draggable_texts")
                        val createdAt = storyJson.getString("created_at") // Fetch created_at timestamp

                        // Convert createdAt string to Date
                        val uploadedAt = parseDate(createdAt)

                        // Create the Story object
                        val story = Story(
                            id = storyId,
                            mediaUrl = mediaUrl,
                            duration = duration,
                            trimmedAudioUrl = trimmedAudioUrl,
                            draggableTexts = draggableTexts,
                            uploadedAt = uploadedAt
                        )

                        storiesList.add(story)
                    }

                    saveStoriesDataToSharedPreference(storiesList)
                    updateStoriesUI(storiesList)

                } catch (e: JSONException) {
                    e.printStackTrace()
                    // Optionally show a Toast message for error feedback
                    // Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching stories: ${error.message}")
                loadStoriesFromSharedPreferences()
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

                        // Parse fields from the JSON response
                        val postId = postJson.getInt("post_id")
                        val postContent = postJson.optString("post_content", null)
                        val caption = postJson.optString("caption", null)
                        val sponsored = postJson.getBoolean("sponsored")
                        val likes = postJson.getInt("likes")
                        val comments = postJson.getInt("post_comments")
                        val shares = postJson.getInt("shares")
                        val clicks = postJson.getInt("clicks")
                        val city = postJson.optString("city", null)
                        val country = postJson.optString("country", null)
                        val trimmedAudioUrl = postJson.optString("trimmed_audio_url", null)
                        val createdAt = postJson.getString("created_at")

                        // Create a Post object
                        val post = Post(
                            postId = postId,
                            postContent = postContent,
                            caption = caption,
                            sponsored = sponsored,
                            likes = likes,
                            comments = comments,
                            shares = shares,
                            clicks = clicks,
                            city = city,
                            country = country,
                            trimmedAudioUrl = trimmedAudioUrl,
                            createdAt = createdAt
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
    private fun updatePostsUI(posts: List<Post>) {
        postsAdapter = PostsAdapter(posts) // Create a new adapter with the fetched posts
        postsRecyclerView.adapter = postsAdapter
        postsCount=findViewById(R.id.postsCount)
        postsCount.setText(postsAdapter.itemCount.toString())// Set the adapter to RecyclerView
    }

    private fun saveRankToPreferences(rank: Int) {
        with(sharedPreferences4.edit()) {
            putInt("userRank", rank)
            apply()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadRankFromPreferences() {
        val rank = sharedPreferences4.getInt("userRank", -1)
        if (rank != -1) {
            rankTextView.text = "Rank: $rank"
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
                    put("storyId", story.id)
                    put("mediaUrl", story.mediaUrl)
                    put("duration", story.duration)
                    put("trimmedAudioUrl", story.trimmedAudioUrl)
                    put("draggableTexts", story.draggableTexts)
                    put("created_at", story.uploadedAt?.time ?: 0L) // Store as timestamp
                })
            }
        }.toString()

        with(sharedPreferences2.edit()) {
            putString("storiesList", storiesJsonArray)
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
                    storyJson.getInt("storyId"),
                    storyJson.getString("mediaUrl"),
                    storyJson.getInt("duration"),
                    storyJson.optString("trimmedAudioUrl", null),
                    storyJson.optJSONArray("draggableTexts"),
                    Date(uploadedAt)
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
                })
            }
        }.toString()

        with(sharedPreferences3.edit()) {
            putString("postsList", postsJsonArray)
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
                val post = Post(
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
                    createdAt = postJson.getString("created_at")
                )
                posts.add(post)
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
                        val storyId = storyJson.getInt("id")
                        val mediaUrl = storyJson.getString("media_url")
                        val duration = storyJson.getInt("duration")
                        val trimmedAudioUrl = storyJson.optString("trimmed_audio_url", null)
                        val draggableTexts = storyJson.optJSONArray("draggable_texts")
                        val createdAt = storyJson.getString("created_at") // Fetch created_at timestamp

                        // Convert createdAt string to Date
                        val uploadedAt = parseDate(createdAt)

                        // Create the Story object
                        val story = Story(
                            id = storyId,
                            mediaUrl = mediaUrl,
                            duration = duration,
                            trimmedAudioUrl = trimmedAudioUrl,
                            draggableTexts = draggableTexts,
                            uploadedAt = uploadedAt
                        )

                        storiesList.add(story)
                    }

                    // Show or hide the story ring based on the presence of valid stories
                    if (storiesList.isNotEmpty()) {
                        // Show the story ring
                        findViewById<ImageView>(R.id.storyRing).visibility = View.VISIBLE

                        // Launch StoryViewActivity if there are stories
                        val intent = Intent(this, viewStory::class.java).apply {
                            putParcelableArrayListExtra("storiesList", ArrayList(storiesList))
                            putExtra("currentIndex", 0)
                        }
                        startActivity(intent)
                    } else {
                        // Hide the story ring if there are no stories
                        findViewById<ImageView>(R.id.storyRing).visibility = View.GONE
                        navigateToFullProfilePicture()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }


    // Helper function to parse the date string
    private fun parseDate(dateString: String): Date? {
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
            format.parse(dateString)
        } catch (e: Exception) {
            e.printStackTrace()
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


