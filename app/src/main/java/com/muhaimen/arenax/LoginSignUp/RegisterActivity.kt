package com.muhaimen.arenax.LoginSignUp

import android.content.Context
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
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.UserData

class RegisterActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var reEnterPasswordEditText: EditText
    private lateinit var googleSignUpButton: Button
    private lateinit var nextBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        reEnterPasswordEditText = findViewById(R.id.reEnterPasswordEditText)
        val loginBtn: TextView = findViewById(R.id.login_button)
        nextBtn = findViewById(R.id.nextButton)
        googleSignUpButton = findViewById(R.id.googleSignUpButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }

        nextBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val reEnterPassword = reEnterPasswordEditText.text.toString()

            if (validateInput(email, password, reEnterPassword)) {
                // Save email and password in SharedPreferences
                saveUserCredentialsToSharedPreferences(email, password)

                // Create a UserData instance and navigate to the next activity
                val user = UserData(
                    userId = "",
                    fullname = "",
                    email = email,
                    password = password,
                    dOB = "",
                    gamerTag = "",
                    profilePicture = null,
                    gender = Gender.PreferNotToSay
                )

                // Navigate to the next activity (PersonalInfoActivity)
                val intent = Intent(this, PersonalInfoActivity::class.java)
                intent.putExtra("userData", user) // Optional: Pass user data if needed
                startActivity(intent)
                finish() // Close RegisterActivity to prevent going back
            }
        }

        // Google Sign Up Button Click
        googleSignUpButton.setOnClickListener {
            // Instead of signing in, directly store the email and password
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Save user credentials in SharedPreferences
            saveUserCredentialsToSharedPreferences(email, password)

            // Create a UserData instance and navigate to the next activity
            val user = UserData(
                userId = "",
                fullname = "",
                email = email,
                password = password,
                dOB = "",
                gamerTag = "",
                profilePicture = null,
                gender = Gender.PreferNotToSay
            )

            // Navigate to the next activity (PersonalInfoActivity)
            val intent = Intent(this, PersonalInfoActivity::class.java)
            intent.putExtra("userData", user) // Optional: Pass user data if needed
            startActivity(intent)
            finish() // Close RegisterActivity to prevent going back
        }
    }

    private fun validateInput(email: String, password: String, reEnterPassword: String): Boolean {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email address"
            return false
        }

        if (password.length < 8) {
            passwordEditText.error = "Password must be at least 8 characters long"
            return false
        }

        if (password != reEnterPassword) {
            reEnterPasswordEditText.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun saveUserCredentialsToSharedPreferences(email: String, password: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("email", email)
            putString("password", password) // Store password as well
            apply()  // Save email and password in SharedPreferences
        }
    }
}
