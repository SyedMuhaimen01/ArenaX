package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R

class manageAdmins : AppCompatActivity() { // Class name should start with uppercase
    private lateinit var adminsRecyclerView: RecyclerView
    private lateinit var superAdminTextView: TextView
    private lateinit var manageAdminsAdapter: ManageAdminsAdapter
    private lateinit var searchAdminsAdapter: SearchAdminsAdapter
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var backButton:ImageButton
    private lateinit var addAdminButton: FloatingActionButton
    private lateinit var searchbarLinearLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_admins)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        adminsRecyclerView = findViewById(R.id.adminsRecyclerview)
        superAdminTextView = findViewById(R.id.superAdminTextView)
        searchRecyclerView = findViewById(R.id.searchAdminsRecyclerView)
        searchBar = findViewById(R.id.searchbar)
        searchbarLinearLayout = findViewById(R.id.searchbarLinearLayout)
        // Initialize searchAdminsAdapter
        searchAdminsAdapter = SearchAdminsAdapter(emptyList())
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchAdminsAdapter

        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                adminsRecyclerView.visibility = View.GONE
                searchRecyclerView.visibility = VISIBLE
            }
        }

        adminsRecyclerView.layoutManager = LinearLayoutManager(this)
        manageAdminsAdapter = ManageAdminsAdapter(emptyList())
        adminsRecyclerView.adapter = manageAdminsAdapter

        addAdminButton=findViewById(R.id.addAdminButton)
        addAdminButton.setOnClickListener {
            if (searchbarLinearLayout.visibility == View.VISIBLE) {
                searchbarLinearLayout.visibility = View.GONE
                searchRecyclerView.visibility = GONE
                adminsRecyclerView.visibility = View.VISIBLE
            } else {
                searchbarLinearLayout.visibility = View.VISIBLE
                searchRecyclerView.visibility = VISIBLE
                adminsRecyclerView.visibility = View.GONE
            }
        }


        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }
}
