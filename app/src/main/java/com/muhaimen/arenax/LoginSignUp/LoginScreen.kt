package com.muhaimen.arenax.LoginSignUp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.MainActivity
import com.muhaimen.arenax.R
import com.muhaimen.arenax.utils.FirebaseManager

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var resetPassword: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.usernameEditText) // Changed to emailEditText if needed
        passwordInput = findViewById(R.id.passwordEditText)
        resetPassword = findViewById(R.id.resetPassword)
        val login_btn: Button = findViewById(R.id.login_button)
        val signup_btn: TextView = findViewById(R.id.signup_button)

        resetPassword.setOnClickListener {
            sendVerificationEmailAndResetPassword()
        }

        login_btn.setOnClickListener {
            loginUser()
        }

        signup_btn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent) // Start the new activity
        }
    }

    private fun sendVerificationEmailAndResetPassword() {
        val email = emailInput.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        // Send a verification email to the entered email address
        FirebaseManager.sendPasswordResetEmail(email) { success, error ->
            if (success) {
                Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show()

                // Move to the ForgotPassword activity
                val intent = Intent(this, ForgotPassword::class.java)
                intent.putExtra("email", email) // Pass the email to ForgotPassword activity
                startActivity(intent)
            } else {
                // Show error if the email sending fails
                Toast.makeText(this, "Error sending verification email: ${error ?: "Unknown error."}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Attempt to sign in with email
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    FirebaseManager.checkEmailVerification() { isVerified, error ->
                        if (isVerified) {
                            // Email is verified, proceed to MainActivity
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish() // Close the login activity
                        } else {
                            // Email is not verified
                            Toast.makeText(this, "Email is not verified. Please verify your email.", Toast.LENGTH_SHORT).show()

                            FirebaseManager.sendVerificationEmail { success, error ->
                                if (success) {
                                    Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Show error if email sending fails
                                    Toast.makeText(this, "Error sending verification email: ${error ?: "Unknown error."}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            navigateToMainActivity()
                        }
                    }
                } else {
                    // Sign in failed
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }

            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fromPersonalInfo", true) // Indicate that it came from PersonalInfoActivity
        startActivity(intent)
        finish() // Close PersonalInfoActivity to prevent going back
    }
}
