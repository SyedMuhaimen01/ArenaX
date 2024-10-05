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
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONObject

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var resetPassword: TextView
    private val TAG = "LoginScreen"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        // Adjust the padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        emailInput = findViewById(R.id.usernameEditText)
        passwordInput = findViewById(R.id.passwordEditText)
        resetPassword = findViewById(R.id.resetPassword)
        val loginBtn: Button = findViewById(R.id.login_button)
        val signupBtn: TextView = findViewById(R.id.signup_button)

        resetPassword.setOnClickListener { sendVerificationEmailAndResetPassword() }
        loginBtn.setOnClickListener { loginUser() }
        signupBtn.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun sendVerificationEmailAndResetPassword() {
        val email = emailInput.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseManager.sendPasswordResetEmail(email) { success, error ->
            if (success) {
                Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ForgotPassword::class.java).apply {
                    putExtra("email", email)
                })
            } else {
                Toast.makeText(this, "Error sending verification email: ${error ?: "Unknown error."}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        Log.d(TAG, "Attempting to log in with email: $email")

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { uid ->
                        Log.d(TAG, "Login successful, User ID: ${uid.uid}")
                        fetchUserData(uid.uid)
                    } ?: run {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Login failed: ${task.exception?.message}")
                }
            }
    }

    private fun fetchUserData(uid: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("userData/$uid")
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val userData = dataSnapshot.getValue(UserData::class.java)
            userData?.let { user ->
                Log.d(TAG, "Fetched user data: $user")
                if (user.accountVerified) {
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
        }
    }

    private fun sendUserDataToServer(userData: UserData) {
        val url = "http://192.168.100.6:3000/api/register" // Your backend endpoint

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
                Toast.makeText(this, "Data successfully stored in Postgres", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "User data successfully sent to server: $response")
            },
            { error ->
                Toast.makeText(this, "Failed to store data: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to store data: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}