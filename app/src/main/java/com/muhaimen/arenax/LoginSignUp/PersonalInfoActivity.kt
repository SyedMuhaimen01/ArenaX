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
    private lateinit var registerBtn: Button
    private var email: String? = null
    private var password: String? = null

    private val TAG = "PersonalInfoActivity"

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
        loadingTextView = findViewById(R.id.loadingTextView)
        registerBtn= findViewById(R.id.register_button)
        handler = Handler(Looper.getMainLooper())

        email = intent.getStringExtra("email") //retrieving data from intent passed from RegisterActivity
        password = intent.getStringExtra("password")

        dOBEditText.setOnClickListener {// Setting up date picker for Date of Birth
            showDatePickerDialog()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val genderOptions = Gender.entries.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter

        registerBtn.setOnClickListener {
            registerUser()
        }
    }

    override fun onResume() {
        super.onResume()
        checkIfRolledBack()
    }

    private fun checkIfRolledBack() {
        if (intent.getBooleanExtra("fromMainActivity", false)) {
            deleteUserNode()
        }
    }

    private fun registerUser() {
        val fullName = fullNameEditText.text.toString().trim()
        val dateOfBirth = dOBEditText.text.toString().trim()
        val gender = Gender.entries[genderSpinner.selectedItemPosition]
        val gamerTag = gamertagEditText.text.toString().trim()

        if (fullName.isEmpty() || dateOfBirth.isEmpty() || gamerTag.isEmpty()) {
            showToast("Please fill all the fields.")
            return
        } else if (gamerTag.length < 3 || gamerTag.length > 15) {
            showToast("Invalid GamerTag Length")
            return
        } else if (fullName.length < 3 || fullName.length > 30) {
            showToast("Invalid Name Length")
            return
        }
        showLoadingUI()

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

        val userData = UserData(
            userId = FirebaseManager.getCurrentUserId() ?: "",
            fullname = fullName,
            gamerTag = gamerTag,
            email = email,
            dOB = dateOfBirth,
            gender = gender,
            accountVerified = false,
            profilePicture = null
        )

        FirebaseManager.signUpUser(email, password, userData) { success, registerError ->
            if (success) {
                showToast("Verification email sent. Please verify your email.")
                Log.d(TAG, "User registered successfully.")
                navigateToMainActivity()
            } else {

                FirebaseManager.checkIfEmailVerified(email, password) { isVerified, _ ->
                    if (isVerified) {
                        showToast("Error registering user: ${registerError ?: "Unknown error."}")
                    } else {
                        showToast("Email is not verified. Please verify your email.")
                        FirebaseManager.sendVerificationEmail { success, error ->
                            if (!success) {
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

    private fun deleteUserNode() {
        FirebaseManager.deleteUserData { success, _ ->
            if (success) {
                showToast("User data deleted due to failure of user to verify email.")
                Log.d(TAG, "User data deleted due to rollback from MainActivity.")
            } else {
                Log.e(TAG, "Error deleting user data")
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fromPersonalInfo", true)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                dOBEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            }, year, month, day
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