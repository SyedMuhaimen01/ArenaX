package com.muhaimen.arenax.accountSettings

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.arenax.LoginSignUp.LoginScreen
import com.muhaimen.arenax.R
import com.muhaimen.arenax.utils.FirebaseManager

class accountSettings : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var logoutButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        backButton = findViewById(R.id.backButton)
        logoutButton = findViewById(R.id.logoutLinearLayout)

        backButton.setOnClickListener {
            finish()
        }
        logoutButton.setOnClickListener {
            FirebaseManager.signOutUser()
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }
    }
}