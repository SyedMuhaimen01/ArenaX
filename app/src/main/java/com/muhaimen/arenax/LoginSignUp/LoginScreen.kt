package com.muhaimen.arenax.LoginSignUp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.os.Bundle
import android.provider.Settings
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
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONObject

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var resetPassword: TextView
    private lateinit var loginBtn: Button
    private lateinit var signupBtn: TextView

    private val TAG = "LoginScreen"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        emailInput = findViewById(R.id.usernameEditText)
        passwordInput = findViewById(R.id.passwordEditText)
        resetPassword = findViewById(R.id.resetPassword)
        loginBtn= findViewById(R.id.login_button)
        signupBtn= findViewById(R.id.signup_button)

        resetPassword.setOnClickListener { sendVerificationEmailAndResetPassword() }
        loginBtn.setOnClickListener { loginUser() }
        signupBtn.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }

        if (!checkUsageStatsPermission()) {
            showUsageStatsPermissionDialog()
        }

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
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { uid -> fetchUserData(uid.uid) } ?: run {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
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
                if (user.accountVerified) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Account is not verified", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "User data not found for ID: $uid")
            }
        }.addOnFailureListener { exception ->
           // Toast.makeText(this, "Failed to fetch user data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun checkUsageStatsPermission(): Boolean {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun showUsageStatsPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Usage Access Required")
            .setMessage("This app needs usage access permission to track screen time of games.\n To enjoy full in-app features" +
                    " please enable Usage Access in the settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
