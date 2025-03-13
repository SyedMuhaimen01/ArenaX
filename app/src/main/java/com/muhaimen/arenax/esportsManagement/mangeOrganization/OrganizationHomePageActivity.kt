package com.muhaimen.arenax.esportsManagement.mangeOrganization

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.databinding.ActivityOrganizationHomePageBinding
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class OrganizationHomePageActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityOrganizationHomePageBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
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
        val navController = findNavController(R.id.nav_host_fragment_content_organization_home_page)

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

        source = intent.getStringExtra("Source")
        organizationName = intent.getStringExtra("organization_name")

        if (source == "MyOrganizationsAdapter") {
            organizationName?.let { binding.appBarOrganizationHomePage.toolbar.title = it }
            navController.navigate(R.id.nav_dashboard)
            fetchOrganizationData()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_organization_home_page)
        val drawerLayout: DrawerLayout = binding.drawerLayout

        when (item.itemId) {
            R.id.nav_dashboard -> navController.navigate(R.id.nav_dashboard)
            R.id.nav_pagePosts -> navController.navigate(R.id.nav_pagePosts)
            R.id.nav_inbox -> navController.navigate(R.id.nav_inbox)
            R.id.nav_organizationTeams -> navController.navigate(R.id.nav_organizationTeams)
            R.id.nav_manageEvents -> navController.navigate(R.id.nav_manageEvents)
            R.id.nav_manageApplications -> navController.navigate(R.id.nav_manageApplications)
            R.id.nav_editPage -> navController.navigate(R.id.nav_editPage)
            R.id.nav_jobs -> navController.navigate(R.id.nav_jobs)
            R.id.nav_sponsoredPosts -> navController.navigate(R.id.nav_sponsoredPosts)
            R.id.nav_settings -> navController.navigate(R.id.nav_settings)
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

    private fun fetchOrganizationData() {
        val url = "${Constants.SERVER_URL}registerOrganization/organization/$organizationName/organizationData"
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    val organization = OrganizationData(
                        organizationId = response.getString("organization_id"),
                        organizationName = response.getString("organization_name"),
                        organizationLogo = response.optString("organization_logo", ""),
                        organizationLocation = response.optString("organization_location", "")
                    )
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing response: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.organization_home_page, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_organization_home_page)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
