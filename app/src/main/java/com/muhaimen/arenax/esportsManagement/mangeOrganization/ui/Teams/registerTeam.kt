package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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

        registerButton.setOnClickListener {
            if (teamLogoUri != null) {
                uploadLogoToFirebase()
            } else {
                sendTeamToBackend("")
            }
            val intent=Intent(this, OrganizationHomePageActivity::class.java)
            startActivity(intent)
        }


    }

    private fun initializeViews() {
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
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            teamLogoUri = data.data
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
    private fun getTeamData(logoUrl: String): Team {
        val enteredGamerTag = teamCaptainEditText.text.toString()

        // Validate before creating the Team object
        if (!userMap.contains(enteredGamerTag)) {
            teamCaptainEditText.error = "Invalid gamer tag! Please select from suggestions."
            return Team() // Return an empty team to avoid incorrect submission
        }

        return Team(
            teamName = teamNameEditText.text.toString(),
            gameName = gameNameEditText.text.toString(),
            teamDetails = teamDetailsEditText.text.toString(),
            teamLocation = teamLocationEditText.text.toString(),
            teamEmail = teamEmailEditText.text.toString(),
            teamCaptain = enteredGamerTag, // Store gamerTag instead of userId
            teamTagLine = teamTagLineEditText.text.toString(),
            teamAchievements = teamAchievementsEditText.text.toString(),
            teamLogo = logoUrl
        )
    }

    private fun sendTeamToBackend(logoUrl: String) {
        val team = getTeamData(logoUrl)

        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("organizationName", organizationName ?: "")
            put("teamName", team.teamName)
            put("gameName", team.gameName)
            put("teamDetails", team.teamDetails)
            put("teamLocation", team.teamLocation)
            put("teamEmail", team.teamEmail)
            put("teamCaptain", team.teamCaptain)
            put("teamTagLine", team.teamTagLine)
            put("teamAchievements", team.teamAchievements)
            put("teamLogo", team.teamLogo) // Adding Firebase URL to request body
        }

        val url = "${Constants.SERVER_URL}manageTeams/addTeam"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                Toast.makeText(this, "Team Registered Successfully!", Toast.LENGTH_LONG).show()
                finish()
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }
}
