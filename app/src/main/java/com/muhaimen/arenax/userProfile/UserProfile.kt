package com.muhaimen.arenax.userProfile

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.uploadStory.uploadStory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.muhaimen.arenax.R
import com.muhaimen.arenax.accountSettings.accountSettings
import com.muhaimen.arenax.dataClasses.AnalyticsData
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.editProfile.editProfile
import com.muhaimen.arenax.gamesDashboard.overallLeaderboard
import com.muhaimen.arenax.gamesDashboard.MyGamesList
import com.muhaimen.arenax.uploadContent.UploadContent
import android.provider.Settings
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.dataClasses.Story
import com.muhaimen.arenax.gamesDashboard.MyGamesListAdapter
import com.muhaimen.arenax.screenTime.ScreenTimeService
import com.muhaimen.arenax.utils.Constants
import highlightsAdapter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class UserProfile : AppCompatActivity() {
    private val USAGE_STATS_PERMISSION_REQUEST_CODE = 1001
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var myGamesListRecyclerView: RecyclerView
    private lateinit var myGamesListAdapter: MyGamesListAdapter
    private lateinit var myGamesList: List<AnalyticsData>
    private lateinit var highlightsRecyclerView: RecyclerView
    private lateinit var highlightsAdapter: highlightsAdapter
    private lateinit var synergyButton:ImageButton
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
    private lateinit var userData: UserData
    private lateinit var settingsButton:Button
    private lateinit var leaderboardButton: ImageButton
    private lateinit var rankTextView: TextView
    private lateinit var requestQueue: RequestQueue
    private val client = OkHttpClient()
    private lateinit var activity : String
    private val sharedPreferences by lazy { getSharedPreferences("MyGamesPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences2 by lazy { getSharedPreferences("MyStoriesPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences3 by lazy { getSharedPreferences("MyPostsPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences4 by lazy { getSharedPreferences("MyRankPrefs", Context.MODE_PRIVATE) }
    private val sharedPreferences5 by lazy { getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE) }
    @SuppressLint("MissingInflatedId", "SetTextI18n")
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
        databaseReference = FirebaseDatabase.getInstance().getReference("userData").child(auth.currentUser?.uid ?: "")
        storageReference = FirebaseStorage.getInstance().reference.child("profileImages/${auth.currentUser?.uid}")
        requestQueue = Volley.newRequestQueue(this)
        activity="UserProfile"
        myGamesListRecyclerView = findViewById(R.id.analytics_recyclerview)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        myGamesListAdapter = MyGamesListAdapter(emptyList())
        myGamesListRecyclerView.adapter = myGamesListAdapter



        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        postsRecyclerView = findViewById(R.id.posts_recyclerview)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)



        if (!checkUsageStatsPermission()) {
            requestUsageStatsPermission()
        } else {
            startTrackingService()
        }

        if (!isConnected()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            loadUserDataFromSharedPreferences()
            loadStoriesFromSharedPreferences()
            loadPostsFromSharedPreferences()
            loadRankFromPreferences()
        } else {
            fetchUserDetailsFromFirebase()
            loadGamesFromPreferences()
            fetchUserStories()
            fetchUserPosts()
            fetchUserRank()
        }

        profileImage = findViewById(R.id.profilePicture)
        bioTextView = findViewById(R.id.bioText)
        showMoreTextView = findViewById(R.id.showMore)

        leaderboardButton = findViewById(R.id.leaderboardButton)
        leaderboardButton.setOnClickListener {
            val intent = Intent(this, overallLeaderboard::class.java)
            startActivity(intent)
        }


        // Initialize the RecyclerView for analytics
        myGamesListRecyclerView = findViewById(R.id.analytics_recyclerview)
        myGamesListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Set an empty adapter initially
        myGamesListAdapter = MyGamesListAdapter(emptyList())
        myGamesListRecyclerView.adapter = myGamesListAdapter


        // Initialize the RecyclerView for highlights
        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        loadGamesFromPreferences()
        fetchUserStories()

        rankTextView = findViewById(R.id.rankTextView)


        uploadStoryButton = findViewById(R.id.uploadStoryButton)
        uploadStoryButton.setOnClickListener {
            val intent = Intent(this, uploadStory::class.java)
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
                 //   Toast.makeText(this@UserProfile,"Failed to fetch games", Toast.LENGTH_SHORT).show()
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
                    //    Toast.makeText(this@UserProfile, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateEmptyGameList() {
        with(sharedPreferences.edit()) {
            putString("gamesList", "[]") // Store empty list as a JSON string
            apply()
        }
        runOnUiThread {
            // Show empty view in the RecyclerView
            myGamesListAdapter.updateGamesList(emptyList())
         //   Toast.makeText(this@UserProfile, "No games found", Toast.LENGTH_SHORT).show() // Optional feedback
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
                AnalyticsData(
                    gameName = gameObject.getString("gameName"),
                    totalHours = gameObject.getInt("totalHours"),
                    iconResId = gameObject.getString("gameIcon"),
                    graphData = emptyList()
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
                        findViewById<TextView>(R.id.bioText).setText(userData.bio) // New line to set bio
                        val bio=userData.bio

                        if (bio != null) {
                            if (bio.length > 50) { // Adjust the character count as needed
                                showMoreTextView.visibility = View.VISIBLE
                            }
                        }
                        showMoreTextView.setOnClickListener {
                            bioTextView.maxLines = Int.MAX_VALUE
                            bioTextView.ellipsize = null
                            showMoreTextView.visibility = View.GONE  // Hide "See More"
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
              //      Toast.makeText(this@UserProfile, "Failed to load user details: ${error.message}", Toast.LENGTH_SHORT).show()
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

                    if(rank===0)
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
        val userId = auth.currentUser?.uid
        val url = "${Constants.SERVER_URL}stories/user/$userId/fetchStory"
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
                        val trimmedAudioUrl = storyJson.optString("trimmed_audio_url", null.toString())
                        val draggableTexts = storyJson.optJSONArray("draggable_texts")

                        val story = Story(storyId, mediaUrl, duration, trimmedAudioUrl, draggableTexts)
                        storiesList.add(story)
                    }
                    saveStoriesDataToSharedPreference(storiesList)
                    updateStoriesUI(storiesList)

                } catch (e: JSONException) {
                    e.printStackTrace()
              //      Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching stories: ${error.message}")
                loadStoriesFromSharedPreferences()
           //     Toast.makeText(this, "Error fetching stories", Toast.LENGTH_SHORT).show()
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
                        val trimmedAudioUrl = postJson.optString("trimmed_audio_url", null.toString())
                        val createdAt = postJson.getString("created_at")
                        val post = Post(postId, postContent, caption, sponsored, likes, comments, shares, clicks, trimmedAudioUrl, createdAt)
                        postsList.add(post)
                    }
                    savePostsDataToSharedPreference(postsList)
                    updatePostsUI(postsList)

                } catch (e: JSONException) {
                    e.printStackTrace()
                //    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching posts: ${error.message}")
                loadPostsFromSharedPreferences()
            //    Toast.makeText(this, "Error fetching posts", Toast.LENGTH_SHORT).show()
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
                val story = Story(
                    storyJson.getInt("storyId"),
                    storyJson.getString("mediaUrl"),
                    storyJson.getInt("duration"),
                    storyJson.optString("trimmedAudioUrl", null.toString()),
                    storyJson.optJSONArray("draggableTexts")
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
                    put("post_content", post.postContent)
                    put("caption", post.caption)
                    put("sponsored", post.sponsored)
                    put("likes", post.likes)
                    put("post_comments", post.comments)
                    put("shares", post.shares)
                    put("clicks", post.clicks)
                    put("trimmed_audio_url", post.trimmedAudioUrl)
                    put("created_at", post.createdAt)
                })
            }
        }.toString()

        with(sharedPreferences3.edit()) {
            putString("postsList", postsJsonArray)
            apply()
        }
    }

    private fun loadPostsFromSharedPreferences() {
        val postsJson = sharedPreferences3.getString("postsList", null)
        if (postsJson != null) {
            val posts = mutableListOf<Post>()
            val jsonArray = JSONArray(postsJson)
            for (i in 0 until jsonArray.length()) {
                val postJson = jsonArray.getJSONObject(i)
                val post = Post(
                    postJson.getInt("post_id"),
                    postJson.optString("post_content", null.toString()),
                    postJson.optString("caption", null.toString()),
                    postJson.getBoolean("sponsored"),
                    postJson.getInt("likes"),
                    postJson.getInt("post_comments"),
                    postJson.getInt("shares"),
                    postJson.getInt("clicks"),
                    postJson.optString("trimmed_audio_url", null.toString()),
                    postJson.getString("created_at")
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
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver2)
    }

}


