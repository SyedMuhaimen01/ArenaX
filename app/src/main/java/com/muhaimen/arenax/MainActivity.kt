package com.muhaimen.arenax

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.arenax.LoginSignUp.RegisterActivity
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.utils.FirebaseManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Delay before checking login status
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserLoginStatus()
        }, 3000)
    }

    private fun checkUserLoginStatus() {
        // Use the isUserLoggedIn() method from FirebaseManager
        if (FirebaseManager.isUserLoggedIn()) {
            // User is logged in, navigate to UserProfile
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        } else {
            // User is not logged in, navigate to RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}
