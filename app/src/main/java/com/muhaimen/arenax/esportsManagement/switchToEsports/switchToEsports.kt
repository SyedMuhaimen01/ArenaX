package com.muhaimen.arenax.esportsManagement.switchToEsports

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.battlegrounds.battlegrounds
import com.muhaimen.arenax.esportsManagement.esportsProfile.esportsProfile
import com.muhaimen.arenax.esportsManagement.exploreEsports.exploreEsports
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
import com.muhaimen.arenax.esportsManagement.mangeOrganization.createOrganization.createOrganization
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange
import com.muhaimen.arenax.userProfile.UserProfile

class switchToEsports : AppCompatActivity() {
    private lateinit var talentExhangeButton : ImageView
    private lateinit var battlegroundsButton : ImageView
    private lateinit var switchButton : ImageView
    private lateinit var exploreButton : ImageView
    private lateinit var profileButton : ImageView
    private lateinit var createOrganizationButton: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_switch_to_esports)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        // button listeners initialization
        talentExhangeButton = findViewById(R.id.talentExchangeButton)
        battlegroundsButton = findViewById(R.id.battlegroundsButton)
        switchButton = findViewById(R.id.switchButton)
        exploreButton = findViewById(R.id.exploreButton)
        profileButton = findViewById(R.id.profileButton)
        createOrganizationButton = findViewById(R.id.createOrganizationButton)

        talentExhangeButton.setOnClickListener {
            val intent = Intent(this, talentExchange::class.java)
            startActivity(intent)
        }

        battlegroundsButton.setOnClickListener {
            val intent = Intent(this, battlegrounds::class.java)
            startActivity(intent)
        }

        switchButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }

        exploreButton.setOnClickListener {
            val intent = Intent(this, exploreEsports::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, OrganizationHomePageActivity::class.java)
            startActivity(intent)
        }

        createOrganizationButton.setOnClickListener {
            val intent = Intent(this, createOrganization::class.java)
            startActivity(intent)
        }
    }
}