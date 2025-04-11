package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject
import java.util.*

class registerTeam : AppCompatActivity() {
    private lateinit var teamLogoImageView: ImageView
    private lateinit var teamNameEditText: EditText
    private lateinit var gameNameEditText: EditText
    private lateinit var teamDetailsEditText: EditText
    private lateinit var teamLocationEditText: EditText
    private lateinit var teamEmailEditText: EditText
    private lateinit var backButton:ImageButton
    private lateinit var teamCaptainEditText: AutoCompleteTextView
    private lateinit var teamTagLineEditText: EditText
    private lateinit var teamAchievementsEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var requestQueue: RequestQueue
    private lateinit var userId: String
    private var organizationName: String? = null
    private var teamLogoUri: Uri? = null // Store selected logo URI
    private val userMap = mutableSetOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_team)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        requestQueue = Volley.newRequestQueue(this)

        organizationName = intent.getStringExtra("organization_name")

        initializeViews()
        setupCaptainAutoComplete()
        // Select Team Logo
        teamLogoImageView.setOnClickListener {
            pickImage()
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
        registerButton.setOnClickListener {
            if (teamLogoUri != null) {
                uploadLogoToFirebase()
            } else {
                sendTeamToBackend("")
            }
        }


    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        teamLogoImageView = findViewById(R.id.teamLogoImageView)
        teamNameEditText = findViewById(R.id.teamNameEditText)
        gameNameEditText = findViewById(R.id.gameNameEditText)
        teamDetailsEditText = findViewById(R.id.teamDetailsEditText)
        teamLocationEditText = findViewById(R.id.teamLocationEditText)
        teamEmailEditText = findViewById(R.id.teamEmailEditText)
        teamCaptainEditText = findViewById(R.id.teamCaptainEditText)
        teamTagLineEditText = findViewById(R.id.teamTagLineEditText)
        teamAchievementsEditText = findViewById(R.id.teamAchievementsEditText)
        registerButton = findViewById(R.id.registerButton)
    }

    private fun pickImage() {
        // Create an intent to pick an image from the gallery
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        // Set the MIME type to filter only images
        intent.type = "image/*"

        // Start the activity for result
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Get the selected image URI
            teamLogoUri = data.data

            // Load the selected image into the ImageView using Glide
            Glide.with(this)
                .load(teamLogoUri)
                .circleCrop()
                .into(teamLogoImageView)
        }
    }

    private fun uploadLogoToFirebase() {
        teamLogoUri?.let { uri ->
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = "team_logos/${UUID.randomUUID()}.jpg"
            val fileRef = storageRef.child(fileName)

            fileRef.putFile(uri)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        sendTeamToBackend(downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload logo: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun setupCaptainAutoComplete() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("userData")
        val gamerTags = mutableListOf<String>()

        // Fetch gamer tags from Firebase
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val user = childSnapshot.getValue(UserData::class.java)
                    user?.gamerTag?.let {
                        gamerTags.add(it)
                        userMap.add(it) // Store gamerTag for validation
                    }
                }

                // Set up AutoCompleteTextView adapter
                val adapter = ArrayAdapter(this@registerTeam, android.R.layout.simple_dropdown_item_1line, gamerTags)
                teamCaptainEditText.setAdapter(adapter)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Validate gamer tag when text changes
        teamCaptainEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // When the user exits the field
                val enteredGamerTag = teamCaptainEditText.text.toString()
                if (enteredGamerTag.isNotEmpty() && !userMap.contains(enteredGamerTag)) {
                    teamCaptainEditText.error = "Gamer tag does not exist!"
                }
            }
        }
    }

    // Modify getTeamData to store gamerTag as teamCaptain
    private fun getTeamData(logoUrl: String): Team? {
        val enteredGamerTag = teamCaptainEditText.text.toString().trim()
        val teamName = teamNameEditText.text.toString().trim()
        val gameName = gameNameEditText.text.toString().trim()
        val teamLocation = teamLocationEditText.text.toString().trim()

        // Validate gamer tag
        if (!userMap.contains(enteredGamerTag)) {
            teamCaptainEditText.error = "Invalid gamer tag! Please select from suggestions."
            return null
        }

        // Validate team name
        if (teamName.isEmpty()) {
            teamNameEditText.error = "Team name is required"
            return null
        }

        // Validate game name
        if (gameName.isEmpty()) {
            gameNameEditText.error = "Game name is required"
            return null
        }

        // Validate team location
        if (teamLocation.isEmpty()) {
            teamLocationEditText.error = "Team location is required"
            return null
        }

        // Validate email
        val teamEmail = teamEmailEditText.text.toString().trim()
        if (teamEmail.isEmpty()) {
            teamEmailEditText.error = "Email is required"
            return null
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(teamEmail).matches()) {
            teamEmailEditText.error = "Please enter a valid email address"
            return null
        }

        // Validate team logo
        if (logoUrl.isEmpty()) {
            Toast.makeText(this, "Team logo is required", Toast.LENGTH_SHORT).show()
            return null
        }

        // If all validations pass, create and return the Team object
        return Team(
            teamName = teamName,
            gameName = gameName,
            teamDetails = teamDetailsEditText.text.toString().trim(),
            teamLocation = teamLocation,
            teamEmail = teamEmail,
            teamCaptain = enteredGamerTag, // Store gamerTag instead of userId
            teamTagLine = teamTagLineEditText.text.toString().trim(),
            teamAchievements = teamAchievementsEditText.text.toString().trim(),
            teamLogo = logoUrl
        )
    }

    private fun sendTeamToBackend(logoUrl: String) {
        // Get validated team data
        val team = getTeamData(logoUrl) ?: run {
            Toast.makeText(this, "Please fix the errors in the form", Toast.LENGTH_SHORT).show()
            return
        }

        // Create JSON request body
        val requestBody = JSONObject().apply {
            put("userId", userId) // Ensure this is not null or empty
            put("organizationName", organizationName ?: "") // Ensure this is not null or empty
            put("teamName", team.teamName)
            put("gameName", team.gameName)
            put("teamDetails", team.teamDetails)
            put("teamLocation", team.teamLocation)
            put("teamEmail", team.teamEmail)
            put("teamCaptain", team.teamCaptain)
            put("teamTagLine", team.teamTagLine)
            put("teamAchievements", team.teamAchievements)
            put("teamLogo", team.teamLogo)
        }

        // Define the URL
        val url = "${Constants.SERVER_URL}manageTeams/addTeam"

        // Create and send the POST request
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                // Success response
                Toast.makeText(this, "Team Registered Successfully!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, OrganizationHomePageActivity::class.java)
                intent.putExtra("organization_name", organizationName)
                startActivity(intent)
            },
            { error ->
                // Error response
                val errorMsg = error.networkResponse?.data?.let { String(it) } ?: error.message
                Log.e("TeamRegistrationError", "Error: $errorMsg")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String, String>()
                headers["Content-Type"] = "application/json" // Set the content type to JSON
                return headers
            }
        }

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest)

        val intent = Intent(this, OrganizationHomePageActivity::class.java)
        intent.putExtra("organization_name", organizationName)
        startActivity(intent)
    }


    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }
}
