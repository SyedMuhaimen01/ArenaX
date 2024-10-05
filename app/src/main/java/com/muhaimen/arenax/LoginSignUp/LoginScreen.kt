package com.muhaimen.arenax.LoginSignUp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.MainActivity
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import org.json.JSONObject
import com.muhaimen.arenax.utils.FirebaseManager

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var resetPassword: TextView

    // Add a TAG for logging
    private val TAG = "LoginScreen"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        signupBtn.setOnClickListener {
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

        // Log the email and password input for debugging
        Log.d(TAG, "Attempting to log in with email: $email")

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Attempt to sign in with email
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    user?.let {
                        val uid = it.uid
                        Log.d(TAG, "Login successful, User ID: $uid")

                        // Fetch user data from Firebase Realtime Database
                        val database = FirebaseDatabase.getInstance()
                        val userRef = database.getReference("userData/$uid")

                        userRef.get().addOnSuccessListener { dataSnapshot ->
                            val userData = dataSnapshot.getValue(UserData::class.java)
                            userData?.let { user ->
                                Log.d(TAG, "Fetched user data: $user")

                                if (user.accountVerified) {
                                    // User is verified, send data to Node.js server
                                    sendUserDataToServer(user)
                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish() // Close the login activity
                                } else {
                                    Toast.makeText(this, "Account is not verified", Toast.LENGTH_SHORT).show()
                                    Log.w(TAG, "Account not verified for user: $uid")
                                }
                            } ?: run {
                                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "User data not found for ID: $uid")
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, "Failed to fetch user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Failed to fetch user data: ${exception.message}")

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
                    Log.e(TAG, "Login failed: ${task.exception?.message}")
                }

            }
    }
    private fun sendUserDataToServer(userData: UserData) {
        // Send the user data to your Node.js server using an HTTP POST request
        val url = "http://192.168.100.6:3000/api/register"// Your backend endpoint

        val jsonObject = JSONObject().apply {
            put("userId", userData.userId)
            put("fullname", userData.fullname)
            put("email", userData.email)
            put("dOB", userData.dOB)
            put("gamerTag", userData.gamerTag)
            put("profilePicture", userData.profilePicture)
            put("gender", userData.gender.toString())
            put("accountVerified", userData.accountVerified)
        }

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, jsonObject,
            { response ->
                // Handle successful response
                Toast.makeText(this, "Data successfully stored in Postgres", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "User data successfully sent to server: $response")
            },
            { error ->
                // Handle error
                Toast.makeText(this, "Failed to store data: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to store data: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fromPersonalInfo", true) // Indicate that it came from PersonalInfoActivity
        startActivity(intent)
        finish() // Close PersonalInfoActivity to prevent going back

    }
}
