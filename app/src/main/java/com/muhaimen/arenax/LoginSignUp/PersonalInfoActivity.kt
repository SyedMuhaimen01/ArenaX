package com.muhaimen.arenax.LoginSignUp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.arenax.MainActivity
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager
import java.util.*

class PersonalInfoActivity : AppCompatActivity() {
    private lateinit var fullNameEditText: EditText
    private lateinit var dOBEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var gamertagEditText: EditText
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var loadingTextView: TextView
    private lateinit var handler: Handler
    private var email: String? = null
    private var password: String? = null

    private val TAG = "PersonalInfoActivity"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_info)

        // Initialize views
        fullNameEditText = findViewById(R.id.fullNameEditText)
        dOBEditText = findViewById(R.id.dateOfBirthEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        gamertagEditText = findViewById(R.id.gamertagEditText)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        loadingTextView = findViewById(R.id.loadingTextView)
        val registerBtn: Button = findViewById(R.id.register_button)

        // Initialize the handler
        handler = Handler(Looper.getMainLooper())

        // Get email and password from Intent
        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")

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

        // Set up register button click listener
        registerBtn.setOnClickListener {
            registerUser()
        }
    }

    override fun onResume() {
        super.onResume()
        checkIfRolledBack()
    }

    private fun checkIfRolledBack() {
        // Check if the activity was started from the MainActivity
        if (intent.getBooleanExtra("fromMainActivity", false)) {
            deleteUserNode()
        }
    }

    private fun registerUser() {
        val fullName = fullNameEditText.text.toString().trim()
        val dateOfBirth = dOBEditText.text.toString().trim()
        val gender = Gender.values()[genderSpinner.selectedItemPosition]
        val gamerTag = gamertagEditText.text.toString().trim()

        // Validate input fields
        if (fullName.isEmpty() || dateOfBirth.isEmpty() || gamerTag.isEmpty()) {
            showToast("Please fill all the fields.")
            return
        } else if (gamerTag.length < 3 || gamerTag.length > 15) {
            showToast("Invalid GamerTag Length")
            return
        } else if (fullName.length < 3 || fullName.length > 20) {
            showToast("Invalid Name Length")
            return
        }

        // Show loading UI
        showLoadingUI()

        // Check if email and password are available
        val email = email ?: run {
            showToast("Error retrieving email.")
            hideLoadingUI()
            return
        }
        val password = password ?: run {
            showToast("Error retrieving password.")
            hideLoadingUI()
            return
        }

        // Proceed with registration
        val userData = UserData(
            userId = FirebaseManager.getCurrentUserId() ?: "",
            fullname = fullName,
            gamerTag = gamerTag,
            email = email,
            dOB = dateOfBirth,
            gender = gender,
            accountVerified = false,
            profilePicture = null // Handle profile picture later
        )

        FirebaseManager.signUpUser(email, password, userData) { success, registerError ->
            if (success) {
                showToast("Verification email sent. Please verify your email.")
                Log.d(TAG, "User registered successfully.")
                navigateToMainActivity()
            } else {

                FirebaseManager.checkIfEmailVerified(email, password) { isVerified, error ->

                    // Check the verification status
                    if (isVerified) {
                        showToast("Error registering user: ${registerError ?: "Unknown error."}")
                    } else {
                        // Email is not verified, handle accordingly
                        showToast("Email is not verified. Please verify your email.")
                        FirebaseManager.sendVerificationEmail { success, error ->
                            if (success) {

                            } else {
                                showToast("Error sending verification email: ${error ?: "Unknown error."}")
                            }
                        }
                        navigateToMainActivity()
                    }
                }


                    Log.e(TAG, "Error registering user")
            }
            hideLoadingUI()
        }
    }


    private fun sendVerificationEmail(email: String, callback: (Boolean, Exception?) -> Unit) {
        FirebaseManager.sendVerificationEmail() { success, error ->

        }
    }

    private fun deleteUserNode() {
        FirebaseManager.deleteUserData { success, error ->
            if (success) {
                showToast("User data deleted due to rollback from MainActivity.")
                Log.d(TAG, "User data deleted due to rollback from MainActivity.")
            } else {
                showToast("Error deleting user data: ${error ?: "Unknown error."}")
                Log.e(TAG, "Error deleting user data")
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fromPersonalInfo", true) // Indicate that it came from PersonalInfoActivity
        startActivity(intent)
        finish() // Close PersonalInfoActivity to prevent going back
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                dOBEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showLoadingUI() {
        loadingProgressBar.visibility = View.VISIBLE
        loadingTextView.visibility = View.VISIBLE
    }

    private fun hideLoadingUI() {
        loadingProgressBar.visibility = View.GONE
        loadingTextView.visibility = View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
