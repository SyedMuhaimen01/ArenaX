package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams

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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONObject

class viewOwnTeam : AppCompatActivity() {
    private lateinit var teamLogoImageView:ImageView
    private lateinit var teamNameTextView:TextView
    private lateinit var gameNameTextView:TextView
    private lateinit var locationTextView:TextView
    private lateinit var taglineTextView:TextView
    private lateinit var teamCaptainTextView:TextView
    private lateinit var teamCaptainProfilePicture:ImageView
    private lateinit var teamDetailsTextView:TextView
    private lateinit var teamEmailTextView:TextView
    private lateinit var teamAchievementsTextView:TextView
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var searchUserRecyclerView: RecyclerView
    private lateinit var searchUserAdapter: SearchPlayerAdapter
    private lateinit var managePlayersAdapter: ManagePlayerAdapter
    private lateinit var backButton:ImageButton
    private lateinit var addPlayerButton:FloatingActionButton
    private lateinit var searchBar: EditText
    private lateinit var database: DatabaseReference
    private lateinit var searchbarLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_own_team)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        initializeUI()
        // Initialize Firebase Database Reference
        database = FirebaseDatabase.getInstance().reference.child("userData")

        managePlayersAdapter = ManagePlayerAdapter(mutableListOf()) { adminId ->
            // Remove admin from organization logic here
        }

        searchUserAdapter = SearchPlayerAdapter(mutableListOf()) { userId ->
            // Add admin to organization logic here
        }

        // Set Adapters
        playersRecyclerView.adapter = managePlayersAdapter
        searchUserRecyclerView.adapter = searchUserAdapter

        // Search Bar Focus Handling
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                playersRecyclerView.visibility = View.GONE
                searchUserRecyclerView.visibility = View.VISIBLE
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
                    searchUserAdapter.updatePlayersList(emptyList())
                }
            }
        })

        // Floating Button Click Handling
        addPlayerButton.setOnClickListener {
            if (searchbarLinearLayout.visibility == View.VISIBLE) {
                searchbarLinearLayout.visibility = View.GONE
                searchUserRecyclerView.visibility = View.GONE
                playersRecyclerView.visibility = View.VISIBLE
            } else {
                searchbarLinearLayout.visibility = View.VISIBLE
                searchUserRecyclerView.visibility = View.VISIBLE
                playersRecyclerView.visibility = View.GONE
            }
        }

        // Back Button Handling
        backButton.setOnClickListener {
            finish()
        }

        // Handle system back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchUserRecyclerView.visibility == View.VISIBLE) {
                    searchUserRecyclerView.visibility = View.GONE
                    playersRecyclerView.visibility = View.VISIBLE
                    searchBar.text.clear()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        teamCaptainTextView.text?.toString()?.takeIf { it.isNotEmpty() }?.let { captainId ->
            database.child(captainId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(UserData::class.java)?.let { user ->
                        Glide.with(this@viewOwnTeam)
                            .load(user.profilePicture ?: R.drawable.battlegrounds_icon_background)
                            .placeholder(R.drawable.battlegrounds_icon_background)
                            .error(R.drawable.battlegrounds_icon_background)
                            .circleCrop()
                            .into(teamCaptainProfilePicture)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if necessary (e.g., log the error)
                }
            })
        }

    }


    private fun fillTeamData(response: JSONObject) {
        teamNameTextView.text = response.getString("teamName")
        gameNameTextView.text = response.getString("gameName")
        locationTextView.text = response.getString("teamLocation")
        taglineTextView.text = response.getString("teamTagLine")
        teamCaptainTextView.text = response.getString("teamCaptain")
        teamEmailTextView.text = response.getString("teamEmail")
        teamAchievementsTextView.text = response.getString("teamAchievements")
        teamDetailsTextView.text = response.getString("teamDetails")

        // Load team logo with Glide
        Glide.with(this)
            .load(response.getString("teamLogo")) // Firebase URL
            .placeholder(R.drawable.battlegrounds_icon_background)
            .error(R.drawable.battlegrounds_icon_background)
            .into(teamLogoImageView)
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
                searchUserAdapter.updatePlayersList(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ManageAdmins", "Search error: ${error.message}")
            }
        })
    }

    fun initializeUI()
    {
        teamLogoImageView = findViewById(R.id.teamLogo)
        teamNameTextView = findViewById(R.id.teamNameTextView)
        gameNameTextView = findViewById(R.id.gameNameTextView)
        locationTextView = findViewById(R.id.teamLocationTextView)
        taglineTextView = findViewById(R.id.teamTaglineTextView)
        teamCaptainTextView = findViewById(R.id.captainNameTextView)
        teamCaptainProfilePicture = findViewById(R.id.teamCaptainProfilePicture)
        teamEmailTextView = findViewById(R.id.teamEmailTextView)
        teamAchievementsTextView = findViewById(R.id.team_achievements_TextView)
        teamDetailsTextView = findViewById(R.id.team_details_TextView)
        playersRecyclerView = findViewById(R.id.playersRecyclerView)
        searchUserRecyclerView = findViewById(R.id.searchUserRecyclerView)
        backButton = findViewById(R.id.backButton)
        addPlayerButton = findViewById(R.id.addPlayerButton)
        searchBar = findViewById(R.id.searchbar)
        searchbarLinearLayout = findViewById(R.id.searchbarLinearLayout)
        playersRecyclerView.layoutManager = LinearLayoutManager(this)
        searchUserRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}