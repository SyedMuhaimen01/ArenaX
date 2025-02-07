package com.muhaimen.arenax.esportsManagement.mangeOrganization

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.muhaimen.arenax.R
import com.muhaimen.arenax.databinding.ActivityOrganizationHomePageBinding
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange

class OrganizationHomePageActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener  {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityOrganizationHomePageBinding

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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_pagePosts,R.id.nav_inbox,R.id.nav_editPage,R.id.nav_jobs,R.id.nav_sponsoredPosts,R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_organization_home_page)
        val drawerLayout: DrawerLayout = binding.drawerLayout // Get the drawer layout

        when (item.itemId) {
            R.id.nav_dashboard -> {
                navController.navigate(R.id.nav_dashboard)
            }
            R.id.nav_pagePosts -> {
                navController.navigate(R.id.nav_pagePosts)
            }
            R.id.nav_inbox -> {
                navController.navigate(R.id.nav_inbox)
            }
            R.id.nav_editPage -> {
                navController.navigate(R.id.nav_editPage)
            }
            R.id.nav_jobs -> {
                navController.navigate(R.id.nav_jobs)
            }
            R.id.nav_sponsoredPosts -> {
                navController.navigate(R.id.nav_sponsoredPosts)
            }
            R.id.nav_settings -> {
                navController.navigate(R.id.nav_settings)
            }
            R.id.nav_exit -> {
                // Show a confirmation dialog before exiting
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
                return true
            }
            else -> return false
        }

        // Close the drawer after navigation
        drawerLayout.closeDrawers()
        return true
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.organization_home_page, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_organization_home_page)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}