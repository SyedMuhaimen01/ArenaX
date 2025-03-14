package com.muhaimen.arenax.esportsManagement.esportsProfile

import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.databinding.ActivityEsportsProfileBinding
import com.muhaimen.arenax.utils.FirebaseManager

class esportsProfile : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityEsportsProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor=resources.getColor(R.color.primaryColor)
        window.navigationBarColor=resources.getColor(R.color.primaryColor)
        binding = ActivityEsportsProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarEsportsProfile.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val headerView= binding.navView.getHeaderView(0)
        val profileImage = headerView.findViewById<ImageView>(R.id.profilePicture)
        val name = headerView.findViewById<TextView>(R.id.fullNameTextView)
        val gamerTag= headerView.findViewById<TextView>(R.id.gamerTagTextView)

        val databaseReference = FirebaseManager.getCurrentUserId()
            ?.let { FirebaseDatabase.getInstance().getReference("userData").child(it) }

        databaseReference?.get()?.addOnSuccessListener { snapshot ->
            // Check if snapshot exists and retrieve the values
            val fullname = snapshot.child("fullname").getValue(String::class.java)
            val gamertag = snapshot.child("gamerTag").getValue(String::class.java)
            val profilePicture= snapshot.child("profilePicture").getValue(String::class.java)

            if (!profilePicture.isNullOrEmpty() && profilePicture != "null") {
                // Load the profile picture using Glide and apply circleCrop
                Glide.with(this)
                    .load(profilePicture)
                    .circleCrop()
                    .into(profileImage)
            } else {
                // Load a default image when profilePicture is empty, null, or "null"
                Glide.with(this)
                    .load(R.drawable.battlegrounds_icon_background)
                    .circleCrop()
                    .into(profileImage)

            }

            // Set the retrieved values to the TextViews
            name.text = fullname ?: "Unknown" // Set a default value if null
            gamerTag.text = gamertag ?: "Unknown" // Set a default value if null
        }


        val navController = findNavController(R.id.nav_host_fragment_content_esports_profile)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_profile,R.id.nav_notifications,R.id.nav_organization,R.id.nav_team,R.id.nav_findTeam
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.esports_profile, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_esports_profile)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}