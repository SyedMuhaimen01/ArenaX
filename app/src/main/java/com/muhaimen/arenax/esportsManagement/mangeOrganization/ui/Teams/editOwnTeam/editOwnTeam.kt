package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.editOwnTeam

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.arenax.R

class editOwnTeam : AppCompatActivity() {

    private lateinit var teamLogoImageView: ImageView
    private lateinit var teamNameEditText: EditText
    private lateinit var gameNameEditText: EditText
    private lateinit var teamDetailsEditText: EditText
    private lateinit var teamLocationEditText: EditText
    private lateinit var teamEmailEditText: EditText
    private lateinit var teamCaptainEditText: EditText
    private lateinit var teamTagLineEditText: EditText
    private lateinit var teamAchievementsEditText: EditText
    private lateinit var updateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_own_team)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        initializeViews()
    }

    fun initializeViews(){
        teamLogoImageView = findViewById(R.id.teamLogoImageView)
        teamNameEditText = findViewById(R.id.teamNameEditText)
        gameNameEditText = findViewById(R.id.gameNameEditText)
        teamDetailsEditText = findViewById(R.id.teamDetailsEditText)
        teamLocationEditText = findViewById(R.id.teamLocationEditText)
        teamEmailEditText = findViewById(R.id.teamEmailEditText)
        teamCaptainEditText = findViewById(R.id.teamCaptainEditText)
        teamTagLineEditText = findViewById(R.id.teamTagLineEditText)
        teamAchievementsEditText = findViewById(R.id.teamAchievementsEditText)
        updateButton = findViewById(R.id.updateButton)
    }
}