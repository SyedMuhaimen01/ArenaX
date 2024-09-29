package com.muhaimen.arenax.LoginSignUp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.utils.FirebaseManager
import java.text.SimpleDateFormat
import java.util.*

class PersonalInfoActivity : AppCompatActivity() {
    private lateinit var fullNameEditText: EditText
    private lateinit var dOBEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var gamertagEditText: EditText
    private lateinit var loadingProgressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_info)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        dOBEditText = findViewById(R.id.dateOfBirthEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        gamertagEditText = findViewById(R.id.gamertagEditText)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        val registerBtn: Button = findViewById(R.id.register_button)

        // Set up date picker for Date of Birth
        dOBEditText.setOnClickListener {
            showDatePickerDialog()
        }

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Create an array of gender options
        val genderOptions = Gender.values().map { it.displayName }

        // Create an ArrayAdapter for the gender spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter

        registerBtn.setOnClickListener {
            handleRegistration()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                dOBEditText.setText(selectedDate)
            }, year, month, day
        )
        datePickerDialog.show()
    }

    private fun handleRegistration() {
        Log.d("PersonalInfoActivity", "Register button clicked")

        // Retrieve email and password from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("email", null)
        val password = sharedPref.getString("password", null)

        // Get other user information
        val fullName = fullNameEditText.text.toString()
        val dob = dOBEditText.text.toString()
        val gamerTag = gamertagEditText.text.toString()

        // Get the selected gender
        val selectedGender = when (genderSpinner.selectedItemPosition) {
            0 -> Gender.MALE
            1 -> Gender.FEMALE
            2 -> Gender.PreferNotToSay
            else -> Gender.PreferNotToSay // Default case
        }

        if (email != null && password != null && validateInput(fullName, dob, gamerTag)) {
            loadingProgressBar.visibility = View.VISIBLE

            // Convert date from DD/MM/YYYY to YYYY-MM-DD
            try {
                val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val dateObj = sdfInput.parse(dob) // This converts the input string to a Date object

                // Correctly format the parsed date object
                val formattedDate = sdfOutput.format(dateObj) // Format the Date object instead

                // Create UserData object
                val user = UserData(
                    userId = "",  // Will be assigned by Firebase
                    fullname = fullName,
                    password = password,
                    gender = selectedGender,
                    email = email,
                    dOB = formattedDate,
                    gamerTag = gamerTag,
                    profilePicture = ""  // Profile picture handled later
                )

                // ... (existing registration code)
            } catch (e: Exception) {
                Log.e("PersonalInfoActivity", "Error parsing date: ${e.message}")
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            } finally {
                loadingProgressBar.visibility = View.GONE
            }
        } else {
            Log.d("PersonalInfoActivity", "Email or password is null, or input validation failed")
        }
    }

    private fun validateInput(fullName: String, dob: String, gamerTag: String): Boolean {
        if (fullName.isEmpty()) {
            fullNameEditText.error = "Full name is required"
            return false
        }
        if (dob.isEmpty()) {
            dOBEditText.error = "Date of birth is required"
            return false
        }
        if (gamerTag.isEmpty()) {
            gamertagEditText.error = "GamerTag is required"
            return false
        }
        return true
    }

    private fun checkEmailVerification(email: String) {
        // Create a runnable that checks for email verification every few seconds
        val handler = Handler(Looper.getMainLooper())
        val checkVerificationRunnable = object : Runnable {
            override fun run() {
                FirebaseManager.checkEmailVerification { isVerified, error ->
                    if (isVerified) {
                        // Stop checking and navigate to UserProfile activity
                        handler.removeCallbacks(this)
                        loadingProgressBar.visibility = View.GONE // Hide loading
                        val intent = Intent(this@PersonalInfoActivity, UserProfile::class.java)
                        startActivity(intent)
                        finish() // Close PersonalInfoActivity
                    } else if (error != null) {
                        Log.e("PersonalInfoActivity", "Error checking verification status: $error")
                        handler.removeCallbacks(this)
                        loadingProgressBar.visibility = View.GONE // Hide loading
                    } else {
                        // Check again after a delay
                        handler.postDelayed(this, 5000) // Check every 5 seconds
                    }
                }
            }
        }

        // Start checking for verification
        handler.post(checkVerificationRunnable)
    }

    private fun deleteUserData(user: UserData) {
        FirebaseManager.deleteUserData(user.userId) { success, error ->
            if (success) {
                Log.d("PersonalInfoActivity", "User data deleted successfully.")
            } else {
                Log.e("PersonalInfoActivity", "Failed to delete user data: $error")
            }
        }
    }
}
