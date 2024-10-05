package com.muhaimen.arenax.LoginSignUp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.R
import com.muhaimen.arenax.utils.FirebaseManager

class ForgotPassword : AppCompatActivity() {

    private lateinit var resendEmail: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // Get the email from the previous activity
        email = intent.getStringExtra("email") ?: ""

        resendEmail = findViewById(R.id.resendEmailButton)

        // Resend verification email when clicked
        resendEmail.setOnClickListener {
            FirebaseManager.sendPasswordResetEmail(email) { success, errorMessage ->
                if (success) {
                    Toast.makeText(this, "Password reset email sent! Check your email.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle the Reset Password button click to navigate to the login screen
        val resetPasswordBtn: Button = findViewById(R.id.resetPasswordBtn)
        resetPasswordBtn.setOnClickListener {
            navigateToLoginScreen()
        }
    }

    // Navigate to the login screen
    private fun navigateToLoginScreen() {
        startActivity(Intent(this, LoginScreen::class.java)) // Change LoginScreen to the actual login activity class name
        finish() // Optionally finish this activity
    }
}
