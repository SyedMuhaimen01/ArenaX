package com.muhaimen.arenax.editProfile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.userProfile.UserProfile

class editProfile : AppCompatActivity() {
    private lateinit var genderSpinner: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
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

        auth= FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("userData").child(auth.currentUser?.uid ?: "")

        if(!isConnected()){
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
        else{
            fetchUserDetailsFromFirebase()
        }

        val editProfileButton = findViewById<Button>(R.id.updateChangesButton)
        editProfileButton.setOnClickListener {
            updateProfile()
        }

        genderSpinner = findViewById(R.id.genderSpinner)
        // Create an array of gender options
        val genderOptions = Gender.values().map { it.displayName }

        // Create an ArrayAdapter for the gender spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun fetchUserDetailsFromFirebase() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            val userRef = databaseReference
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("fullname").getValue(String::class.java)
                        val gamertag = snapshot.child("gamerTag").getValue(String::class.java)
                        val gender = snapshot.child("gender").getValue(String::class.java)
                        // val imageUrl = snapshot.child("picture").getValue(String::class.java)

                        Log.d("EditUserProfile", "Data loaded from Firebase")

                        name?.let { findViewById<EditText>(R.id.nameEditText).setText(it) }
                        gamertag?.let { findViewById<EditText>(R.id.gamertagEditText).setText(it) }
                        gender?.let { genderValue ->
                            val spinner = findViewById<Spinner>(R.id.genderSpinner)
                            val adapter = spinner.adapter
                            val position = (0 until adapter.count).firstOrNull { index ->
                                adapter.getItem(index)?.toString().equals(genderValue, ignoreCase = true)
                            } ?: 0 // Default to 0 if not found
                            spinner.setSelection(position)
                        }

                        // imageUrl?.let { url ->
                       //     Glide.with(this@editPatientProfile)
                        //        .load(url)
                        //        .circleCrop() // Circle crop the image
                         //       .into(profileImage)
                        }
                    }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        }
    }

    private fun updateProfile() {
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val gamertagEditText = findViewById<EditText>(R.id.gamertagEditText)
        val genderSpinner = findViewById<Spinner>(R.id.genderSpinner)

        val name = nameEditText.text.toString().trim()
        val gamertag = gamertagEditText.text.toString().trim()
        val gender = Gender.values()[genderSpinner.selectedItemPosition]

        if (name.isEmpty() || gamertag.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the gamertag is unique
        databaseReference.child("users").orderByChild("gamertag").equalTo(gamertag)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Gamertag already exists, show error message
                        Toast.makeText(this@editProfile, "Gamertag already taken. Please choose another one.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Gamertag is unique, proceed with the update
                        val userUpdates: MutableMap<String, Any> = HashMap()
                        userUpdates["gamerTag"] = gamertag
                        userUpdates["gender"] = gender.toString()  // assuming Gender is an enum
                        userUpdates["fullname"] = name

                        // Update the user's data in the database
                        databaseReference.updateChildren(userUpdates)
                       Toast.makeText(this@editProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        redirect()

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors
                    Toast.makeText(this@editProfile, "Failed to check gamertag: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun redirect(){
        val intent = Intent(this, UserProfile::class.java)
        startActivity(intent)
    }



}