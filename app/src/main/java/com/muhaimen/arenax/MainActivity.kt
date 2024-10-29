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
import com.muhaimen.arenax.LoginSignUp.LoginScreen
import com.muhaimen.arenax.LoginSignUp.PersonalInfoActivity
import com.muhaimen.arenax.LoginSignUp.RegisterActivity
import com.muhaimen.arenax.explore.ExplorePage
import com.muhaimen.arenax.userFeed.UserFeed
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.utils.FirebaseManager

class MainActivity : AppCompatActivity() {
    private lateinit var handler: Handler
    private var isFromPersonalInfo: Boolean = false
    private val emailVerificationCooldown: Long = 60 * 1000
    private var endTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isFromPersonalInfo = intent.getBooleanExtra("fromPersonalInfo", false)
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            checkUserLoginStatus()
        }, 1000)
    }

    private fun checkUserLoginStatus() {
        if (FirebaseManager.isUserLoggedIn()) {
            if (isFromPersonalInfo) {
                verifyEmailInLoop()
            } else {
                checkEmailVerificationAndNavigate()
            }
        } else {
            navigateToLoginActivity() //if user is not registered , navigate to Register activity
        }
    }

    private fun checkEmailVerificationAndNavigate() {
        FirebaseManager.checkEmailVerification { isVerified, _ ->
            if (isVerified) {
                // User is logged in and email is verified, navigate to UserProfile
                FirebaseManager.updateUserEmailVerificationStatus() { success, errorMessage ->
                    if (success) {
                        Log.d("FirebaseManager", " email verification status Updated Successfully")
                    } else {
                        Log.e("FirebaseManager", "Failed to update email verification status: $errorMessage")
                    }
                }
                navigateToUserProfile() // Navigate to UserProfile upon successful login
            } else {
                navigateToRegisterActivity()     // User is logged in but email is not verified, navigate to RegisterActivity
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
                        Log.d("FirebaseManager", " email verification status Updated Successfully")
                    } else {
                        Log.e("FirebaseManager", "Failed to update email verification status: $errorMessage")
                    }
                }
                navigateToUserProfile()
            } else {
                if (System.currentTimeMillis() < endTime) {
                    handler.postDelayed({ checkEmailVerification() }, 5000) // Check every 5 seconds
                } else {
                    navigateToPersonalInfo()
                }
            }
        }
    }

    private fun navigateToUserProfile() {
        val intent = Intent(this,UserProfile::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginScreen::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToPersonalInfo() {
        val intent = Intent(this, PersonalInfoActivity::class.java)
        intent.putExtra("fromMainActivity", true)
        startActivity(intent)
        finish()
    }
}
