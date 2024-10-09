package com.muhaimen.arenax.userProfile

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
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
import com.jjoe64.graphview.series.DataPoint
import com.muhaimen.arenax.R
import com.muhaimen.arenax.accountSettings.accountSettings
import com.muhaimen.arenax.dataClasses.AnalyticsData
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.editProfile.editProfile
import com.muhaimen.arenax.gamesDashboard.overallLeaderboard
import com.muhaimen.arenax.gamesDashboard.MyGamesList
import com.muhaimen.arenax.uploadContent.UploadContent
import android.provider.Settings
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.dataClasses.Story
import com.muhaimen.arenax.screenTime.ScreenTimeService
import org.json.JSONException


class UserProfile : AppCompatActivity() {
    private val USAGE_STATS_PERMISSION_REQUEST_CODE = 1001
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var analyticsRecyclerView: RecyclerView
    private lateinit var analyticsAdapter: AnalyticsAdapter
    private lateinit var highlightsRecyclerView: RecyclerView
    private lateinit var highlightsAdapter: HighlightsAdapter
    private lateinit var synergyButton:ImageButton
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

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        // Set window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("userData").child(auth.currentUser?.uid ?: "")
        storageReference = FirebaseStorage.getInstance().reference.child("profileImages/${auth.currentUser?.uid}")
        profileImage = findViewById(R.id.profilePicture)

        // Initialize the TextViews
        bioTextView = findViewById(R.id.bioText)
        showMoreTextView = findViewById(R.id.showMore)

        leaderboardButton = findViewById(R.id.leaderboardButton)
        leaderboardButton.setOnClickListener {
            val intent = Intent(this, overallLeaderboard::class.java)
            startActivity(intent)
        }

        rankTextView = findViewById(R.id.rankTextView)
        requestQueue = Volley.newRequestQueue(this)
        fetchUserRank()


        if (!checkUsageStatsPermission()) {
            requestUsageStatsPermission()
        } else {
            startTrackingService() // Start the service if permission is granted

        }


        if (!isConnected()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        } else {
            fetchUserDetailsFromFirebase()
        }


        uploadStoryButton = findViewById(R.id.uploadStoryButton)
        uploadStoryButton.setOnClickListener {
            val intent = Intent(this, uploadStory::class.java)
            startActivity(intent)
        }
       
        // Initialize the RecyclerView for analytics
        analyticsRecyclerView = findViewById(R.id.analytics_recyclerview)
        analyticsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Load sample data into the analytics adapter
        val sampleData = loadSampleAnalyticsData()
        analyticsAdapter = AnalyticsAdapter(sampleData)
        analyticsRecyclerView.adapter = analyticsAdapter

        // Initialize the RecyclerView for highlights
        highlightsRecyclerView = findViewById(R.id.highlights_recyclerview)
        highlightsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchUserStories()



        // Initialize the RecyclerView for posts
        postsRecyclerView = findViewById(R.id.posts_recyclerview)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)

        // Load sample data into the posts adapter
        val samplePosts = loadSamplePostsData()
        postsAdapter = PostsAdapter(samplePosts)
        postsRecyclerView.adapter = postsAdapter

        // Initialize the Edit Profile button
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

                        // Check the length of the bio text to determine if "See More" should be shown
                        if (bio != null) {
                            if (bio.length > 50) { // Adjust the character count as needed
                                showMoreTextView.visibility = View.VISIBLE
                            }
                        }

                        showMoreTextView.setOnClickListener {
                            // Expand bio to show full text
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
                    } else {
                        Log.w("UserProfile", "No data found for user ID: $uid")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserProfile, "Failed to load user details: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditUserProfile", "Database error: ${error.message}")
                }
            })
        }
    }
    // Sample function to load analytics data
    private fun loadSampleAnalyticsData(): List<AnalyticsData> {
        // Example data points for graph (Hours vs Days)
        val hoursData1 = listOf(
            DataPoint(1.0, 2.0),
            DataPoint(2.0, 3.0),
            DataPoint(3.0, 5.0),
            DataPoint(4.0, 7.0),
            DataPoint(5.0, 4.0)
        )
        val hoursData2 = listOf(
            DataPoint(1.0, 1.0),
            DataPoint(2.0, 2.5),
            DataPoint(3.0, 4.5),
            DataPoint(4.0, 6.0),
            DataPoint(5.0, 5.0)
        )

        // Create AnalyticsData instances
        val game1 = AnalyticsData(
            gameName = "Game 1",
            totalHours = 15,
            iconResId = R.drawable.game_icon_foreground.toString(),
            graphData = hoursData1
        )

        val game2 = AnalyticsData(
            gameName = "Game 2",
            totalHours = 20,
            iconResId = R.drawable.game_icon_foreground.toString(),
            graphData = hoursData2
        )

        // Return a list of analytics data
        return listOf(game1, game2)
    }

    // Sample function to load posts data
    private fun loadSamplePostsData(): List<Post> {
        return listOf(
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground),
            Post(imageResId = R.drawable.profile_icon_foreground)
        )
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
        val url = "http://192.168.100.6:3000/leaderboard/user/${auth.currentUser?.uid}/rank"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    // Extract rank from the response
                    val rank = response.getInt("rank")
                    rankTextView.text = "User Rank: $rank"
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching rank: ${error.message}")
                Toast.makeText(this, "Error fetching rank", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchUserStories() {
        val userId = auth.currentUser?.uid // Get the current user's ID
        val url = "http://192.168.100.6:3000/stories/user/$userId/fetchStory"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    // Initialize a list to hold the stories
                    val storiesList = mutableListOf<Story>() // Assuming you have a Story data class

                    // Loop through the JSON array to extract stories
                    for (i in 0 until response.length()) {
                        val storyJson = response.getJSONObject(i)
                        // Extract story details, assuming the structure from your backend
                        val storyId = storyJson.getInt("id")
                        val mediaUrl = storyJson.getString("media_url")
                        val duration = storyJson.getInt("duration")
                        val trimmedAudioUrl = storyJson.optString("trimmed_audio_url", null)
                        val draggableTexts = storyJson.optJSONArray("draggable_texts")

                        // Create a Story object and add it to the list
                        val story = Story(storyId, mediaUrl, duration, trimmedAudioUrl, draggableTexts)
                        storiesList.add(story)
                    }

                    // Update UI with the retrieved stories
                    // For example, populate a RecyclerView or any other UI component
                    updateStoriesUI(storiesList)

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                Log.e(TAG, "Error fetching stories: ${error.message}")
                Toast.makeText(this, "Error fetching stories", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest)
    }

    // Function to update UI with the fetched stories
    private fun updateStoriesUI(stories: List<Story>) {
        highlightsAdapter = HighlightsAdapter(stories) // Create a new adapter with fetched stories
        highlightsRecyclerView.adapter = highlightsAdapter // Set the adapter to RecyclerView
    }


}


