package com.muhaimen.arenax.esportsManagement.switchToEsports

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.esportsProfile.esportsProfile
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
import com.muhaimen.arenax.userProfile.UserProfile

class switchToEsports : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch_to_esports)

        // Set colors for status bar and navigation bar
        window.statusBarColor = resources.getColor(R.color.primaryColor, theme)
        window.navigationBarColor = resources.getColor(R.color.primaryColor, theme)

        val switchingText1: TextView = findViewById(R.id.switchingText1)
        val lottieAnimation: LottieAnimationView = findViewById(R.id.lottieAnimation)

        var isEsportsProfile=false
        val loadedFromActivity=intent.getStringExtra("loadedFromActivity")
        if (loadedFromActivity=="casual")
        {
            isEsportsProfile=true
        }
        else if (loadedFromActivity=="esports")
        {
            isEsportsProfile=false
        }
        else
        {

        }
        // Decide which text to show
        if (isEsportsProfile) {
            switchingText1.text="⚡ Game On! Entering Pro Mode..."
        } else {
            switchingText1.text="🌟 Wind Down &amp; Game On—Casual Mode Loading!"
        }

        // Fade-in animation for text
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }
        if (isEsportsProfile) {
            switchingText1.startAnimation(fadeIn)
        } else {
            switchingText1.startAnimation(fadeIn)
        }

        // Start Lottie animation
        lottieAnimation.playAnimation()

        // Delay transition to next activity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (isEsportsProfile) {
                Intent(this, esportsProfile::class.java)
            } else {
                Intent(this, UserProfile::class.java)
            }
            startActivity(intent)
            finish()
        }, 1500)

    }

}
