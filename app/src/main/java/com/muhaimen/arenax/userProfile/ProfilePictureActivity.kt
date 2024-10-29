package com.muhaimen.arenax.userProfile

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R

class ProfilePictureActivity : AppCompatActivity() {
    private lateinit var fullProfileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_picture)

        fullProfileImageView = findViewById(R.id.fullProfileImageView)

        // Get the URL from the intent and load the image
        val profilePictureUrl = intent.getStringExtra("profilePictureUrl")
        if (!profilePictureUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profilePictureUrl)
                .into(fullProfileImageView)
        } else {
            // Handle case where the URL is empty
            fullProfileImageView.setImageResource(R.drawable.circle)
        }
    }
}
