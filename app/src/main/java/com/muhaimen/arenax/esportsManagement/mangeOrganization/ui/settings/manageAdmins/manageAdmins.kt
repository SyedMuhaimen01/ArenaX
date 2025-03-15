package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager

class manageAdmins : AppCompatActivity() {
    private lateinit var adminsRecyclerView: RecyclerView
    private lateinit var superAdminTextView: TextView
    private lateinit var superAdminProfilePicture: ImageView
    private lateinit var manageAdminsAdapter: ManageAdminsAdapter
    private lateinit var searchAdminsAdapter: SearchAdminsAdapter
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var backButton: ImageButton
    private lateinit var addAdminButton: FloatingActionButton
    private lateinit var searchbarLinearLayout: LinearLayout
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_admins)

        // Set up system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        // Initialize Firebase Database Reference
        database = FirebaseDatabase.getInstance().reference.child("userData")

        // Initialize views
        adminsRecyclerView = findViewById(R.id.adminsRecyclerview)
        superAdminTextView = findViewById(R.id.superAdminTextView)
        superAdminProfilePicture = findViewById(R.id.superAdminProfilePicture)
        searchRecyclerView = findViewById(R.id.searchAdminsRecyclerView)
        searchBar = findViewById(R.id.searchbar)
        searchbarLinearLayout = findViewById(R.id.searchbarLinearLayout)
        addAdminButton = findViewById(R.id.addAdminButton)
        backButton = findViewById(R.id.backButton)

        // Initialize RecyclerViews
        adminsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Adapters
        manageAdminsAdapter = ManageAdminsAdapter(mutableListOf()) { adminId ->
            // Remove admin from organization logic here
        }

        searchAdminsAdapter = SearchAdminsAdapter(mutableListOf()) { userId ->
            // Add admin to organization logic here
        }

        // Set Adapters
        adminsRecyclerView.adapter = manageAdminsAdapter
        searchRecyclerView.adapter = searchAdminsAdapter

        // Search Bar Focus Handling
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                adminsRecyclerView.visibility = View.GONE
                searchRecyclerView.visibility = View.VISIBLE
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.trim() ?: ""
                if (searchText.isNotEmpty()) {
                    searchUsers(searchText)
                } else {
                    searchAdminsAdapter.updateAdminsList(emptyList())
                }
            }
        })

        // Floating Button Click Handling
        addAdminButton.setOnClickListener {
            if (searchbarLinearLayout.visibility == View.VISIBLE) {
                searchbarLinearLayout.visibility = View.GONE
                searchRecyclerView.visibility = View.GONE
                adminsRecyclerView.visibility = View.VISIBLE
            } else {
                searchbarLinearLayout.visibility = View.VISIBLE
                searchRecyclerView.visibility = View.VISIBLE
                adminsRecyclerView.visibility = View.GONE
            }
        }

        // Back Button Handling
        backButton.setOnClickListener {
            finish()
        }

        // Handle system back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchRecyclerView.visibility == View.VISIBLE) {
                    searchRecyclerView.visibility = View.GONE
                    adminsRecyclerView.visibility = View.VISIBLE
                    searchBar.text.clear()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("userData")
        FirebaseManager.getCurrentUserId()?.let {
            databaseRef.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserData::class.java)
                    if (user != null) {
                        superAdminTextView.text = user.fullname
                        Glide.with(this@manageAdmins)
                            .load(user.profilePicture ?: R.drawable.battlegrounds_icon_background)
                            .placeholder(R.drawable.battlegrounds_icon_background)
                            .error(R.drawable.battlegrounds_icon_background)
                            .circleCrop()
                            .into(superAdminProfilePicture)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if necessary
                }
            })
        }
    }

    private fun searchUsers(query: String) {
        val usersList = mutableListOf<UserData>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserData::class.java)
                    if (user != null && user.userId != FirebaseManager.getCurrentUserId()) {
                        if (user.fullname.contains(query, ignoreCase = true) ||
                            user.gamerTag.contains(query, ignoreCase = true)
                        ) {
                            if (!usersList.any { it.userId == user.userId }) {
                                usersList.add(user)
                            }
                        }
                    }
                }
                searchAdminsAdapter.updateAdminsList(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ManageAdmins", "Search error: ${error.message}")
            }
        })
    }
}
