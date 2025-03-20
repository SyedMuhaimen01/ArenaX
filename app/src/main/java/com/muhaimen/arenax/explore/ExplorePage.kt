package com.muhaimen.arenax.explore

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.gamesDashboard.MyGamesList
import com.muhaimen.arenax.uploadContent.UploadContent
import com.muhaimen.arenax.userFeed.UserFeed
import com.muhaimen.arenax.userProfile.UserProfile

class ExplorePage : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var addPost: ImageView
    private lateinit var homeButton: LinearLayout
    private lateinit var profileButton: ImageView
    private lateinit var talentExchangeButton:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_explore_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)
        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)

        // Initialize TabLayout and ViewPager2
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = true
        // Set up ViewPager2 with an adapter (replace with your adapter)
        viewPager.adapter = ExplorePageViewPagerAdapter(this)

        // Attach TabLayout with ViewPager2 using TabLayoutMediator
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Posts"
                1 -> "Accounts"
                else -> null

            }
        }.attach()

        // Setup click listeners for the bottom navigation buttons
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {

        homeButton= findViewById(R.id.home)
        homeButton.setOnClickListener {
            val intent = Intent(this, UserFeed::class.java)
            startActivity(intent)
        }


        addPost= findViewById(R.id.addPostButton)
        addPost.setOnClickListener {
            val intent = Intent(this, UploadContent::class.java)
            startActivity(intent)
        }

        profileButton= findViewById(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }

        talentExchangeButton=findViewById(R.id.talentExchangeButton)
        talentExchangeButton.setOnClickListener {
            val intent = Intent(this, switchToEsports::class.java)
            intent.putExtra("loadedFromActivity","casual")
            startActivity(intent)
        }

    }
}
