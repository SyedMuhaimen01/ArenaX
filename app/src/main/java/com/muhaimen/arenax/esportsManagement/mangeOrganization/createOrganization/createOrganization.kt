package com.muhaimen.arenax.esportsManagement.mangeOrganization.createOrganization

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

class createOrganization : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var organizationNameEditText: EditText
    private lateinit var organizationLocation: EditText
    private lateinit var organizationEmail: EditText
    private lateinit var organizationPhone: EditText
    private lateinit var organizationWebsite: EditText
    private lateinit var industryType: Spinner
    private lateinit var organizationType: Spinner
    private lateinit var organizationSize: Spinner
    private lateinit var organizationTagline: EditText
    private lateinit var organizationDescription: EditText
    private lateinit var organizationLogo: ImageView
    private lateinit var submitButton: Button
    private lateinit var requestQueue: RequestQueue
    private lateinit var database: FirebaseDatabase
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var user: String
    var imageUrl: String? = null
    var imageUrl2: String = ""
    var imageUri: Uri? = null
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
        industryType = findViewById(R.id.organizationIndustryEditText)
        organizationWebsite = findViewById(R.id.organizationWebsiteEditText)
        organizationPhone = findViewById(R.id.organizationPhoneEditText)
        organizationEmail = findViewById(R.id.organizationEmailEditText)
        organizationType = findViewById(R.id.organizationTypeSpinner)
        organizationSize = findViewById(R.id.organizationSizeSpinner)
        organizationTagline = findViewById(R.id.organizationTaglineEditText)
        organizationDescription = findViewById(R.id.organizationDescriptionEditText)
        organizationLogo = findViewById(R.id.organizationLogoImage)
        organizationLogo.setImageURI(null)
        submitButton = findViewById(R.id.submitButton)
        ArrayAdapter.createFromResource(
            this, R.array.organization_types, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            organizationType.adapter = adapter
        }
        ArrayAdapter.createFromResource(
            this, R.array.organization_industry_types, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            industryType.adapter = adapter
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
            submitButton.isEnabled = false
            val organizationData = getOrganizationDataFromInputs()
            if (organizationData != null) {
                saveOrganizationToFirebase(organizationData)
            }
            else {
                submitButton.isEnabled = true
            }
        }
    }

    @SuppressLint("IntentReset")
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*" //
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
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
        val industry = industryType.selectedItem.toString().trim()
        val type = organizationType.selectedItem?.toString()?.trim()
        val size = organizationSize.selectedItem?.toString()?.trim()
        val description = organizationDescription.text.toString().trim()
        val website=organizationWebsite.text.toString().trim()
        // Check for empty fields
        if (name.isEmpty()) {
            organizationNameEditText.error = "Organization name is required"
            organizationNameEditText.requestFocus()
            return null
        }
        if (email.isEmpty()) {
            organizationEmail.error = "Email is required"
            return null
        }

        // Use Android's built-in email pattern to validate the input
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            organizationEmail.error = "Please enter a valid email address"
            return null
        }
        if(imageUri == null) {
            Toast.makeText(this, "Please select an organization logo", Toast.LENGTH_SHORT).show()
            return null
        }
        if (location.isEmpty()) {
            organizationLocation.error = "Location is required"
            organizationLocation.requestFocus()
            return null
        }
        if (phone.isEmpty() || (phone.length != 11 && phone.length != 9) ) {
            organizationPhone.error = "Phone number is required"
            organizationPhone.requestFocus()
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

        if (website.isEmpty()) {
            organizationWebsite.error = "Website URL is required"
            organizationWebsite.requestFocus()
            return null
        }
        if (!android.util.Patterns.WEB_URL.matcher(website).matches()) {
            organizationWebsite.error = "Please enter a valid website URL"
            organizationWebsite.requestFocus()
            return null
        }

        return OrganizationData(
            organizationName = name,
            organizationLocation = location,
            organizationEmail = email,
            organizationPhone = phone,
            organizationWebsite = website,
            organizationIndustry = industry,
            organizationType = type,
            organizationSize = size,
            organizationTagline = organizationTagline.text.toString().trim(),
            organizationDescription = description,
            organizationLogo = imageUri.toString()
        )
    }

    private fun uploadImageToFirebase(organizationId: String,organization: OrganizationData) {
        storageReference = FirebaseStorage.getInstance("gs://i210888.appspot.com").reference.child("organizationContent/$organizationId/organizationProfilePictures")

        if (imageUri != null) {
            // Generate a unique filename for the image to avoid overwriting
            val fileName = "organizationProfile_${organizationId}.jpg"
            val fileReference: StorageReference = storageReference.child(fileName)

            // Upload the image file to Firebase Storage
            fileReference.putFile(imageUri!!)
                .addOnSuccessListener {
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    // Optionally, you can get the download URL after upload
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        imageUrl = uri.toString()
                        // Save the image URL to the database
                        val databaseRef = FirebaseDatabase.getInstance().getReference("organizationsData")
                        databaseRef.child(organizationId).child("organizationLogo").setValue(imageUrl)
                            .addOnSuccessListener {

                                organization.organizationLogo = imageUrl
                                sendOrganizationDataToServer(organization)
                            }
                            .addOnFailureListener { e ->
                                println("Failed to save image URL: ${'$'}{e.message}")
                            }


                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }


    }

    fun saveOrganizationToFirebase(organization: OrganizationData){
        val databaseRef = FirebaseDatabase.getInstance().getReference("organizationsData")
        val organizationId = databaseRef.push().key ?: return // Generate unique ID

        val organizationObject = OrganizationData(
            organizationId = organizationId,
            organizationName = organization.organizationName,
            organizationLocation = organization.organizationLocation,
            organizationEmail = organization.organizationEmail,
            organizationPhone = organization.organizationPhone,
            organizationWebsite = organization.organizationWebsite,
            organizationIndustry = organization.organizationIndustry,
            organizationType = organization.organizationType,
            organizationSize = organization.organizationSize,
            organizationTagline = organization.organizationTagline,
            organizationDescription = organization.organizationDescription,
            organizationLogo = organization.organizationLogo
        )

        databaseRef.child(organizationId).setValue(organizationObject)
            .addOnSuccessListener {
                println("Organization saved successfully")
            }
            .addOnFailureListener { e ->
                println("Failed to save organization: ${'$'}{e.message}")
            }

        uploadImageToFirebase(organizationId,organization)

    }


    private fun sendOrganizationDataToServer(organization: OrganizationData) {
        val url = "${Constants.SERVER_URL}registerOrganization/user/${user}/register"
        val jsonObject = JSONObject().apply {
            put("organizationName", organization.organizationName)
            put("organizationLogo", imageUrl)
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
                    submitButton.isEnabled = true
                    Toast.makeText(this, "Organization Created Successfully!", Toast.LENGTH_SHORT).show()
                    val intent=Intent(this, OrganizationHomePageActivity::class.java)
                    intent.putExtra("organization_name",organization.organizationName)
                    startActivity(intent)
                } else {

                    val errorMessage = response.optString("error", "Unknown error occurred.")
                    if (errorMessage.contains("Organization name already exists", ignoreCase = true)) {
                        Toast.makeText(this, "Error: Organization name already exists. Please choose a unique name.", Toast.LENGTH_LONG).show()
                    } else {
                        //Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                    submitButton.isEnabled = true
                }
            },
            { error ->
                //Toast.makeText(this, "🌐 Network Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(request)
    }
}
