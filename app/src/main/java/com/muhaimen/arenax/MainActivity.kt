package com.muhaimen.arenax

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.arenax.LoginSignUp.PersonalInfoActivity
import com.muhaimen.arenax.LoginSignUp.RegisterActivity
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.utils.FirebaseManager

class MainActivity : AppCompatActivity() {
    private lateinit var handler: Handler
    private var isFromPersonalInfo: Boolean = false
    private val emailVerificationCooldown: Long = 60 * 1000 // 1 minute
    private var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Check if launched from PersonalInfoActivity
        isFromPersonalInfo = intent.getBooleanExtra("fromPersonalInfo", false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        handler = Handler(Looper.getMainLooper())

        // Delay before checking login status
        handler.postDelayed({
            checkUserLoginStatus()
        }, 3000)
    }

    private fun checkUserLoginStatus() {
        // Check if user is logged in
        if (FirebaseManager.isUserLoggedIn()) {
            // User is logged in, check email verification based on the launch context
            if (isFromPersonalInfo) {
                // If launched from PersonalInfoActivity, start the verification loop
                verifyEmailInLoop()
            } else {
                // Regular login check
                checkEmailVerificationAndNavigate()
            }
        } else {
            // User is not logged in, navigate to RegisterActivity
            navigateToRegisterActivity()
        }
    }

    private fun checkEmailVerificationAndNavigate() {
        FirebaseManager.checkEmailVerification { isVerified, errorMessage ->
            if (isVerified) {
                // User is logged in and email is verified, navigate to UserProfile
                FirebaseManager.updateUserEmailVerificationStatus() { success, errorMessage ->
                    if (success) {
                        Log.e("FirebaseManager", " email verification status Updated Successfully")
                        // Handle UI update or other logic if needed
                    } else {
                        // Failed to update email verification status
                        Log.e("FirebaseManager", "Failed to update email verification status: $errorMessage")
                        // Handle error scenario
                    }
                }
                navigateToUserProfile()
            } else {
                // User is logged in but email is not verified, navigate to RegisterActivity
                navigateToRegisterActivity()
            }
        }
    }

    private fun verifyEmailInLoop() {
        endTime = System.currentTimeMillis() + emailVerificationCooldown
        checkEmailVerification()
    }

    private fun checkEmailVerification() {
        FirebaseManager.checkEmailVerification { isVerified, errorMessage ->
            if (isVerified) {
                // Email is verified, navigate to UserProfile
                FirebaseManager.updateUserEmailVerificationStatus() { success, errorMessage ->
                    if (success) {
                        Log.e("FirebaseManager", " email verification status Updated Successfully")
                        // Handle UI update or other logic if needed
                    } else {
                        // Failed to update email verification status
                        Log.e("FirebaseManager", "Failed to update email verification status: $errorMessage")
                        // Handle error scenario
                    }
                }
                navigateToUserProfile()
            } else {
                // Continue checking if within the 1-minute (60 seconds)
                if (System.currentTimeMillis() < endTime) {
                    handler.postDelayed({ checkEmailVerification() }, 5000) // Check every 5 seconds
                } else {
                    // Timeout reached, roll back to PersonalInfoActivity
                    navigateToPersonalInfo()
                }
            }
        }
    }

    private fun navigateToUserProfile() {
        val intent = Intent(this, UserProfile::class.java)
        startActivity(intent)
        finish() // Close MainActivity to prevent going back
    }

    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish() // Close MainActivity to prevent going back
    }

    private fun navigateToPersonalInfo() {
        val intent = Intent(this, PersonalInfoActivity::class.java) // Navigate to RegisterActivity for user data
        intent.putExtra("fromMainActivity", true) // Optional: pass an extra flag if needed
        startActivity(intent)
        finish() // Close MainActivity to prevent going back
    }
}
