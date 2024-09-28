package com.muhaimen.arenax

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val login_btn:TextView= findViewById(R.id.login_button)
        val next_btn:Button= findViewById(R.id.next_button)

        login_btn.setOnClickListener {
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent) // Start the new activity
        }

        next_btn.setOnClickListener {
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent) // Start the new activity
        }
    }
}