package com.muhaimen.arenax.esportsManagement.talentExchange

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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
import com.muhaimen.arenax.esportsManagement.exploreEsports.exploreEsports
import com.muhaimen.arenax.esportsManagement.exploreEsports.exploreEsportsViewPagerAdapter
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.userProfile.UserProfile

class talentExchange : AppCompatActivity() {

    private lateinit var talentExchangeButton : ImageView
    private lateinit var battlegroundsButton : ImageView
    private lateinit var switchButton : ImageView
    private lateinit var exploreButton : ImageView
    private lateinit var profileButton : ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_talent_exchange)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        // button listeners initialization
        talentExchangeButton = findViewById(R.id.talentExchangeButton)
        battlegroundsButton = findViewById(R.id.battlegroundsButton)
        switchButton = findViewById(R.id.switchButton)
        exploreButton = findViewById(R.id.exploreButton)
        profileButton = findViewById(R.id.profileButton)

        // Initialize TabLayout and ViewPager2
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = true

        viewPager.adapter = talentExchangeViewPagerAdapter(this)

        // Attach TabLayout with ViewPager2 using TabLayoutMediator
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Employees"
                1 -> "Organizations"
                else -> null

            }
        }.attach()

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
            intent.putExtra("loadedFromActivity", "esports")
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