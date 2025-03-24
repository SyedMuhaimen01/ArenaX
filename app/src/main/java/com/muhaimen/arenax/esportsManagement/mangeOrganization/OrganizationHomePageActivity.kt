package com.muhaimen.arenax.esportsManagement.mangeOrganization

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.databinding.ActivityOrganizationHomePageBinding
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

class OrganizationHomePageActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityOrganizationHomePageBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var user: String
    private var source: String? = null
    private var organizationName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrganizationHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarOrganizationHomePage.toolbar)

        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val headerView= binding.navView.getHeaderView(0)

        val profileImage = headerView.findViewById<ImageView>(R.id.imageView)
        val name = headerView.findViewById<TextView>(R.id.organizationNameTextView)
        val website = headerView.findViewById<TextView>(R.id.textView)
        organizationName = intent.getStringExtra("organization_name")
        name.text = organizationName
        val databaseRef = FirebaseDatabase.getInstance().getReference("organizationsData")
        val query = databaseRef.orderByChild("organizationName").equalTo(organizationName)

        query.get().addOnSuccessListener { snapshot ->
            for (data in snapshot.children) {
                val organization = data.getValue(OrganizationData::class.java)
                // Assuming email is a TextView and profileImage is an ImageView
                website.text = organization?.organizationWebsite

                val logo = organization?.organizationLogo
                if (!logo.isNullOrEmpty() && logo != "null") {
                    Glide.with(this)
                        .load(logo)
                        .circleCrop()
                        .into(profileImage)

                } else {
                    Glide.with(this).load(R.drawable.battlegrounds_icon_background).into(profileImage)
                }

            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseError", "Error fetching organization data", exception)
        }



        navController = findNavController(R.id.nav_host_fragment_content_organization_home_page)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_pagePosts, R.id.nav_inbox, R.id.nav_editPage,
                R.id.nav_jobs, R.id.nav_sponsoredPosts, R.id.nav_settings, R.id.nav_manageEvents,
                R.id.nav_manageApplications, R.id.nav_organizationTeams, R.id.nav_exit
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser?.uid ?: ""

        // Retrieve organization name from Intent
        source = intent.getStringExtra("Source")
        organizationName = intent.getStringExtra("organization_name")
        Log.d("OrganizationHomePageActivity", "Organization name: $organizationName")

        // Set the title and navigate to Dashboard if coming from MyOrganizationsAdapter
        if (source == "MyOrganizationsAdapter") {
            organizationName?.let { binding.appBarOrganizationHomePage.toolbar.title = it }
            navigateToFragment(R.id.nav_dashboard)
            fetchOrganizationData()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawerLayout: DrawerLayout = binding.drawerLayout

        when (item.itemId) {
            R.id.nav_dashboard -> navigateToFragment(R.id.nav_dashboard)
            R.id.nav_pagePosts -> navigateToFragment(R.id.nav_pagePosts)
            R.id.nav_inbox -> navigateToFragment(R.id.nav_inbox)
            R.id.nav_organizationTeams -> navigateToFragment(R.id.nav_organizationTeams)
            R.id.nav_manageEvents -> navigateToFragment(R.id.nav_manageEvents)
            R.id.nav_manageApplications -> navigateToFragment(R.id.nav_manageApplications)
            R.id.nav_editPage -> navigateToFragment(R.id.nav_editPage)
            R.id.nav_jobs -> navigateToFragment(R.id.nav_jobs)
            R.id.nav_sponsoredPosts -> navigateToFragment(R.id.nav_sponsoredPosts)
            R.id.nav_settings -> navigateToFragment(R.id.nav_settings)
            R.id.nav_exit -> {
                val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                builder.setTitle("Exit")
                builder.setMessage("Are you sure you want to exit?")
                builder.setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(this, talentExchange::class.java)
                    startActivity(intent)
                    finish()
                }
                builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                builder.show()
            }
        }

        drawerLayout.closeDrawers()
        return true
    }

    private fun navigateToFragment(destinationId: Int) {
        val bundle = Bundle()
        organizationName?.let { bundle.putString("organization_name", it) }

        navController.navigate(destinationId, bundle, NavOptions.Builder().setLaunchSingleTop(true).build())
    }

    private fun fetchOrganizationData() {
        val url = "${Constants.SERVER_URL}registerOrganization/basicOrganizationData"
        val requestQueue = Volley.newRequestQueue(this)

        // Creating JSON object with organization name
        val requestBody = JSONObject().apply {
            put("organization_name", organizationName)
        }

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    val organization = OrganizationData(
                        organizationName = response.optString("organization_name", ""),
                        organizationLogo = response.optString("organization_logo", null),
                        organizationLocation = response.optString("organization_location", null)
                    )
                    } catch (e: Exception) {
                }
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    "Error ${it.statusCode}: ${String(it.data)}"
                } ?: error.message
                Toast.makeText(this, "Failed to fetch data: $errorMessage", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Content-Type" to "application/json") // Set headers
            }
        }

        // Add request to queue
        requestQueue.add(jsonObjectRequest)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.organization_home_page, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
