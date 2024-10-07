package com.muhaimen.arenax.userProfile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.muhaimen.arenax.LoginSignUp.LoginScreen
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.AnalyticsData
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.editProfile.editProfile
import com.muhaimen.arenax.uploadContent.UploadContent

class UserProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var analyticsRecyclerView: RecyclerView
    private lateinit var analyticsAdapter: AnalyticsAdapter

    private lateinit var highlightsRecyclerView: RecyclerView
    private lateinit var highlightsAdapter: HighlightsAdapter

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var profileImage: ImageView
    private lateinit var bioTextView: TextView
    private lateinit var showMoreTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var addPost: ImageButton
    private lateinit var userData: UserData
    @SuppressLint("MissingInflatedId")
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


        if (!isConnected()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        } else {
            fetchUserDetailsFromFirebase()
        }


        showMoreTextView.setOnClickListener {
            // Expand bio to show full text
            bioTextView.maxLines = Int.MAX_VALUE
            bioTextView.ellipsize = null
            showMoreTextView.visibility = View.GONE  // Hide "See More"
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

        // Load sample data into the highlights adapter
        val sampleHighlights = loadSampleHighlightsData()
        highlightsAdapter = HighlightsAdapter(sampleHighlights)
        highlightsRecyclerView.adapter = highlightsAdapter

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
            iconResId = R.drawable.game_icon_foreground,
            hoursData = hoursData1
        )

        val game2 = AnalyticsData(
            gameName = "Game 2",
            totalHours = 20,
            iconResId = R.drawable.game_icon_foreground,
            hoursData = hoursData2
        )

        // Return a list of analytics data
        return listOf(game1, game2)
    }

    // Sample function to load highlights data
    private fun loadSampleHighlightsData(): List<Highlight> {
        return listOf(
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 1"),
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 2"),
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 3"),
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 4"),
            Highlight(imageResId = R.drawable.profile_icon_foreground, title = "Highlight 5")
        )
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


}
