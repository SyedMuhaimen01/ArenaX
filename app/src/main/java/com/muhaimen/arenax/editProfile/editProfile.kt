package com.muhaimen.arenax.editProfile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private lateinit var userData: UserData
    private lateinit var backBUtton:ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("userData").child(auth.currentUser?.uid ?: "")
        storageReference = FirebaseStorage.getInstance().reference.child("profileImages/${auth.currentUser?.uid}")

        profileImage = findViewById(R.id.ProfilePicture)
        editProfileImage = findViewById(R.id.editProfilePictureText)
        backBUtton=findViewById(R.id.backButton)
        editProfileImage.setOnClickListener { selectImage() }

        if (!isConnected()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        } else {
            fetchUserDetailsFromFirebase()
        }
    backBUtton.setOnClickListener {
            finish()
        }
        val editProfileButton = findViewById<Button>(R.id.updateChangesButton)
        editProfileButton.setOnClickListener {
            updateProfile()
        }

        genderSpinner = findViewById(R.id.genderSpinner)
        val genderOptions = Gender.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

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
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userData = snapshot.getValue(UserData::class.java) ?: UserData()
                        Log.d("EditUserProfile", "Data loaded from Firebase: $userData")

                        findViewById<EditText>(R.id.nameEditText).setText(userData.fullname)
                        findViewById<EditText>(R.id.gamertagEditText).setText(userData.gamerTag)
                        findViewById<EditText>(R.id.bioEditText).setText(userData.bio) // New line to set bio
                        genderSpinner.setSelection(Gender.values().indexOf(userData.gender))

                        userData.profilePicture?.let { url ->
                            Glide.with(this@editProfile)
                                .load(url)
                                .circleCrop()
                                .into(profileImage)
                        }
                    } else {
                        Log.w("EditUserProfile", "No data found for user ID: $uid")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@editProfile, "Failed to load user details: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditUserProfile", "Database error: ${error.message}")
                }
            })
        }
    }

    private fun updateProfile() {
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val gamertagEditText = findViewById<EditText>(R.id.gamertagEditText)
        val bioEditText = findViewById<EditText>(R.id.bioEditText) // Add bioEditText reference
        val genderValue = Gender.values()[genderSpinner.selectedItemPosition]

        val name = nameEditText.text.toString().trim()
        val gamertag = gamertagEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim() // Get the bio text

        if (name.isEmpty() || gamertag.isEmpty() || bio.isEmpty()) { // Check if bio is empty
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("EditProfile", "Checking if gamertag $gamertag is taken")
        databaseReference.child("users").orderByChild("gamerTag").equalTo(gamertag)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(this@editProfile, "Gamertag already taken. Please choose another one.", Toast.LENGTH_SHORT).show()
                        Log.w("EditProfile", "Gamertag $gamertag is already taken")
                    } else {
                        val updatedUserData = userData.copy(
                            fullname = name,
                            gamerTag = gamertag,
                            gender = genderValue,
                            bio = bio
                        )

                        Log.d("EditProfile", "Updating user profile: $updatedUserData")
                        databaseReference.setValue(updatedUserData).addOnCompleteListener {
                            Toast.makeText(this@editProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            uploadImageToFirebase() // Upload the image after updating the profile
                            saveUserDataToPostgreSQL(updatedUserData) // Save to PostgreSQL
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

    private fun saveUserDataToPostgreSQL(userData: UserData) {
        CoroutineScope(Dispatchers.IO).launch {
            val jsonData = JSONObject().apply {
                put("userId", userData.userId) // Include the Firebase user ID
                put("fullname", userData.fullname)
                put("gamerTag", userData.gamerTag)
                put("gender", userData.gender.displayName)
                put("bio", userData.bio)
            }


            val url = "http://192.168.100.6:3000/api2/updateUser"
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
        startActivity(intent)
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
