package com.muhaimen.arenax.esportsManagement.mangeOrganization.createOrganization

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

class createOrganization : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var organizationNameEditText: EditText
    private lateinit var organizationLocation: EditText
    private lateinit var organizationEmail: EditText
    private lateinit var organizationPhone: EditText
    private lateinit var organizationWebsite: EditText
    private lateinit var organizationIndustry: EditText
    private lateinit var organizationType: Spinner
    private lateinit var organizationSize: Spinner
    private lateinit var organizationTagline: EditText
    private lateinit var organizationDescription: EditText
    private lateinit var organizationLogo: ImageView
    private lateinit var submitButton: Button
    private lateinit var requestQueue: RequestQueue
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var user: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_organization)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        requestQueue = Volley.newRequestQueue(this)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser?.uid ?: ""

        organizationNameEditText = findViewById(R.id.organizationNameEditText)
        organizationLocation = findViewById(R.id.organizationLocationEditText)
        organizationEmail = findViewById(R.id.organizationEmailEditText)
        organizationIndustry = findViewById(R.id.organizationIndustryEditText)
        organizationWebsite = findViewById(R.id.organizationWebsiteEditText)
        organizationPhone = findViewById(R.id.organizationPhoneEditText)
        organizationEmail = findViewById(R.id.organizationEmailEditText)
        organizationType = findViewById(R.id.organizationTypeSpinner)
        organizationSize = findViewById(R.id.organizationSizeSpinner)
        organizationTagline = findViewById(R.id.organizationTaglineEditText)
        organizationDescription = findViewById(R.id.organizationDescriptionEditText)
        organizationLogo = findViewById(R.id.organizationLogoImage)
        submitButton = findViewById(R.id.submitButton)
        ArrayAdapter.createFromResource(
            this, R.array.organization_types, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            organizationType.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.organization_sizes, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            organizationSize.adapter = adapter
        }

        organizationLogo.setOnClickListener { selectImage() }

        backButton=findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        submitButton.setOnClickListener {
            val organizationData = getOrganizationDataFromInputs()
            if (organizationData != null) {
                sendOrganizationDataToServer(organizationData)
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            organizationLogo.setImageURI(imageUri)
            organizationLogo.clipToOutline = true
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }


    private fun getOrganizationDataFromInputs(): OrganizationData? {
        val name = organizationNameEditText.text.toString().trim()
        val location = organizationLocation.text.toString().trim()
        val email = organizationEmail.text.toString().trim()
        val phone = organizationPhone.text.toString().trim()
        val industry = organizationIndustry.text.toString().trim()
        val type = organizationType.selectedItem?.toString()?.trim()
        val size = organizationSize.selectedItem?.toString()?.trim()
        val description = organizationDescription.text.toString().trim()

        // Check for empty fields
        if (name.isEmpty()) {
            organizationNameEditText.error = "Organization name is required"
            organizationNameEditText.requestFocus()
            return null
        }
        if (location.isEmpty()) {
            organizationLocation.error = "Location is required"
            organizationLocation.requestFocus()
            return null
        }
        if (email.isEmpty()) {
            organizationEmail.error = "Email is required"
            organizationEmail.requestFocus()
            return null
        }
        if (phone.isEmpty()) {
            organizationPhone.error = "Phone number is required"
            organizationPhone.requestFocus()
            return null
        }
        if (industry.isEmpty()) {
            organizationIndustry.error = "Industry is required"
            organizationIndustry.requestFocus()
            return null
        }
        if (type.isNullOrEmpty()) {
            Toast.makeText(this, "Please select an organization type", Toast.LENGTH_SHORT).show()
            return null
        }
        if (size.isNullOrEmpty()) {
            Toast.makeText(this, "Please select an organization size", Toast.LENGTH_SHORT).show()
            return null
        }

        return OrganizationData(
            organizationName = name,
            organizationLocation = location,
            organizationEmail = email,
            organizationPhone = phone,
            organizationWebsite = organizationWebsite.text.toString().trim(),
            organizationIndustry = industry,
            organizationType = type,
            organizationSize = size,
            organizationTagline = organizationTagline.text.toString().trim(),
            organizationDescription = description,
            organizationLogo = organizationLogo.toString()
        )
    }


    private fun sendOrganizationDataToServer(organization: OrganizationData) {
        val url = "${Constants.SERVER_URL}registerOrganization/user/${user}/register"

        val jsonObject = JSONObject().apply {
            put("organizationName", organization.organizationName)
            put("organizationLogo", organization.organizationLogo)
            put("organizationDescription", organization.organizationDescription)
            put("organizationLocation", organization.organizationLocation)
            put("organizationEmail", organization.organizationEmail)
            put("organizationPhone", organization.organizationPhone)
            put("organizationWebsite", organization.organizationWebsite)
            put("organizationType", organization.organizationType)
            put("organizationIndustry", organization.organizationIndustry)
            put("organizationSize", organization.organizationSize)
            put("organizationTagline", organization.organizationTagline)
            put("organizationOwner", organization.organizationOwner)
            put("organizationMembers", organization.organizationMembers)
            put("organizationAdmins", organization.organizationAdmins)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                val success = response.optBoolean("success", false)
                val message = response.optString("message", "Something went wrong")

                if (success) {
                    Toast.makeText(this, "Organization Created Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    if (message.contains("Organization name already exists", ignoreCase = true)) {
                        Toast.makeText(this, "Error: Organization name already exists. Please choose a unique name.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                    }
                }
            },
            { error ->
                Toast.makeText(this, "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(request)
    }

}
