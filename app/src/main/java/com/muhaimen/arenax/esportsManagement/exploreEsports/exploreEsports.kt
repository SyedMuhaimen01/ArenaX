package com.muhaimen.arenax.esportsManagement.exploreEsports

import android.content.Intent
import android.os.Bundle
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
import com.muhaimen.arenax.esportsManagement.battlegrounds.battlegrounds
import com.muhaimen.arenax.esportsManagement.esportsProfile.esportsProfile
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange
import com.muhaimen.arenax.userProfile.UserProfile

class exploreEsports : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private lateinit var talentExchangeButton : LinearLayout
    private lateinit var battlegroundsButton : LinearLayout
    private lateinit var switchButton : LinearLayout
    private lateinit var exploreButton : LinearLayout
    private lateinit var profileButton : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_explore_esports)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        // Initialize TabLayout and ViewPager2
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = true
        // Set up ViewPager2 with an adapter (replace with your adapter)
        viewPager.adapter = exploreEsportsViewPagerAdapter(this)

        // Attach TabLayout with ViewPager2 using TabLayoutMediator
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Employees"
                1 -> "Organizations"
                else -> null

            }
        }.attach()

        // button listeners initialization
        talentExchangeButton = findViewById(R.id.talentExchangeButton)
        battlegroundsButton = findViewById(R.id.battlegroundsButton)
        switchButton = findViewById(R.id.esportsButton)
        exploreButton = findViewById(R.id.searchButton)
        profileButton = findViewById(R.id.profileButton)

        talentExchangeButton.setOnClickListener {
            val intent = Intent(this, talentExchange::class.java)
            startActivity(intent)
        }

        battlegroundsButton.setOnClickListener {
            val intent = Intent(this, battlegrounds::class.java)
            startActivity(intent)
        }

        switchButton.setOnClickListener {
            val intent = Intent(this, switchToEsports::class.java)
            startActivity(intent)
        }

        exploreButton.setOnClickListener {
            val intent = Intent(this, exploreEsports::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, esportsProfile::class.java)
            startActivity(intent)
        }
    }
}