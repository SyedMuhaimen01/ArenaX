package com.muhaimen.arenax.editProfile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

class editProfile : AppCompatActivity() {
    private lateinit var genderSpinner: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var profileImage: ImageView
    private lateinit var editProfileImage: TextView
    private var imageUri: Uri? = null
    private lateinit var userId: String
    private lateinit var userData: UserData
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val sharedPreferences5 by lazy { getSharedPreferences("UserInfoPrefs", Context.MODE_PRIVATE) }

    private lateinit var backBUtton:ImageView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("userData").child(auth.currentUser?.uid ?: "")
        storageReference = FirebaseStorage.getInstance("gs://i210888.appspot.com").reference.child("profileImages/${auth.currentUser?.uid}")

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        profileImage = findViewById(R.id.ProfilePicture)
        editProfileImage = findViewById(R.id.editProfilePictureText)
        backBUtton=findViewById(R.id.backButton)
        editProfileImage.setOnClickListener { selectImage() }
        genderSpinner = findViewById(R.id.genderSpinner)
        val genderOptions = Gender.entries.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter

        if(sharedPreferences5.getString("userId", "") == "") {
            fetchUserDetailsFromFirebase()
        }else{
            loadUserDataFromSharedPreferences()
        }
        backBUtton.setOnClickListener {
            finish()
        }
        val editProfileButton = findViewById<Button>(R.id.updateChangesButton)
        editProfileButton.setOnClickListener {
            updateProfile()
        }

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primaryColor)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            if(sharedPreferences5.getString("userId", "") == "") {
                fetchUserDetailsFromFirebase()
            }else{
                loadUserDataFromSharedPreferences()
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            profileImage.setImageURI(imageUri)
        }
    }

    private fun uploadImageToFirebase() {
        if (imageUri != null) {
            val fileReference: StorageReference = storageReference.child("profile.jpg")
            fileReference.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        updateProfilePictureUrl(uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfilePictureUrl(imageUrl: String) {
        Log.d("EditProfile", "Updating profile picture URL: $imageUrl")
        databaseReference.child("profilePicture").setValue(imageUrl)
            .addOnCompleteListener {
                updateProfilePictureUrlOnBackend(userId, imageUrl)
                Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile picture: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun fetchUserDetailsFromFirebase() {

        if(isConnected()) {
            userId.let { uid ->
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            userData = snapshot.getValue(UserData::class.java) ?: UserData()
                            val gender = userData.gender.name
                            populateUIWithUserData(
                                userId = userId,
                                fullname = userData.fullname,
                                gamerTag = userData.gamerTag,
                                gender = gender,
                                bio = userData.bio,
                                profilePicture = userData.profilePicture
                            )
                            swipeRefreshLayout.isRefreshing = false
                            saveUserDataToSharedPreferences(userData)
                        } else {
                            Log.w("EditUserProfile", "No data found for user ID: $uid")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@editProfile,
                            "Failed to load user details: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("EditUserProfile", "Database error: ${error.message}")
                    }
                })
            }
        }else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile() {
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val gamertagEditText = findViewById<EditText>(R.id.gamertagEditText)
        val bioEditText = findViewById<EditText>(R.id.bioEditText)
        val genderValue = Gender.entries[genderSpinner.selectedItemPosition]
        val name = nameEditText.text.toString().trim()
        val gamertag = gamertagEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        if (name.isEmpty() || gamertag.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }
        databaseReference.child("users").orderByChild("gamerTag").equalTo(gamertag)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(this@editProfile, "Gamertag already taken. Please choose another one.", Toast.LENGTH_SHORT).show()
                    } else {
                        val updatedUserData = userData.copy(
                            fullname = name,
                            gamerTag = gamertag,
                            gender = genderValue,
                            bio = bio
                        )

                        databaseReference.setValue(updatedUserData).addOnCompleteListener {
                            Toast.makeText(this@editProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            uploadImageToFirebase()
                            saveUserDataToPostgreSQL(updatedUserData)
                            saveUserDataToSharedPreferences(updatedUserData)
                            redirect()
                        }.addOnFailureListener { e ->
                            Log.e("EditProfile", "Failed to update user profile: ${e.message}")
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@editProfile, "Failed to check gamertag: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfile", "Failed to check gamertag: ${databaseError.message}")
                }
            })
    }

    private fun updateProfilePictureUrlOnBackend(userId: String, imageUrl: String) {
        val url = "${Constants.SERVER_URL}api2/user/$userId/updateProfilePicture"

        val jsonData = JSONObject().apply {
            put("profilePictureUrl", imageUrl)
        }

        val jsonObjectRequest = JsonObjectRequest(
            com.android.volley.Request.Method.POST, url, jsonData,
            { _ -> Log.d("EditProfile", "Profile picture URL updated on backend")},
            { error -> Log.e("EditProfile", "Failed to update profile picture URL on backend: ${error.message}")}
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    private fun saveUserDataToPostgreSQL(userData: UserData) {
        CoroutineScope(Dispatchers.IO).launch {
            val jsonData = JSONObject().apply {
                put("userId", userData.userId)
                put("fullname", userData.fullname)
                put("gamerTag", userData.gamerTag)
                put("gender", userData.gender.displayName)
                put("bio", userData.bio)
            }

            val url = "${Constants.SERVER_URL}api2/updateUser"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .post(jsonData.toString().toRequestBody("application/json".toMediaType()))
                .build()
            try {
                val response: Response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("EditProfile", "User data saved to PostgreSQL successfully")
                    } else {
                        Log.e("EditProfile", "Failed to save user data to PostgreSQL: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProfile", "Exception while saving user data to PostgreSQL: ${e.message}")
            }
        }
    }

    private fun redirect() {
        val intent = Intent(this, UserProfile::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity((intent).putExtra("Activity", "EditProfile"))
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveUserDataToSharedPreferences(userData: UserData) {
        val editor = sharedPreferences5.edit()
        editor.putString("userId", userData.userId)
        editor.putString("fullname", userData.fullname)
        editor.putString("gamerTag", userData.gamerTag)
        editor.putString("gender", userData.gender.toString())
        editor.putString("bio", userData.bio)
        editor.putString("profilePicture", userData.profilePicture)
    }

    private fun loadUserDataFromSharedPreferences() {
        val userId = sharedPreferences5.getString("userId", "")
        val fullname = sharedPreferences5.getString("fullname", "")
        val gamerTag = sharedPreferences5.getString("gamerTag", "")
        val bio = sharedPreferences5.getString("bio", "")
        val profilePicture = sharedPreferences5.getString("profilePicture", "")
        val savedGender = sharedPreferences5.getString("gender", "")
        val genderIndex = Gender.entries.indexOfFirst { it.name == savedGender }
        if (genderIndex != -1) {
            genderSpinner.setSelection(genderIndex)
        }

        if (!userId.isNullOrEmpty() && !fullname.isNullOrEmpty() && !gamerTag.isNullOrEmpty()) {
            populateUIWithUserData(userId, fullname, gamerTag, savedGender, bio, profilePicture)
        }
    }

    private fun populateUIWithUserData(
        userId: String?,
        fullname: String?,
        gamerTag: String?,
        gender: String?,
        bio: String?,
        profilePicture: String?
    ) {
        swipeRefreshLayout.isRefreshing = false
        findViewById<EditText>(R.id.nameEditText).setText(fullname)
        findViewById<EditText>(R.id.gamertagEditText).setText(gamerTag)
        findViewById<EditText>(R.id.bioEditText).setText(bio)
        if (gender != null) {
            val genderIndex = Gender.entries.indexOfFirst { it.name == gender }
            if (genderIndex != -1) {
                genderSpinner.setSelection(genderIndex)
            }
        }
        profilePicture?.let { profilePictureUrl ->
            Glide.with(this@editProfile)
                .load(profilePictureUrl)
                .circleCrop()
                .into(profileImage)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}