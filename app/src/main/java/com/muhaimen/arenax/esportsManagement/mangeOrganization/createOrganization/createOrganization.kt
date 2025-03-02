package com.muhaimen.arenax.esportsManagement.mangeOrganization.createOrganization

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData

class createOrganization : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var organizationNameEditText : EditText
    private lateinit var organizationLocation : EditText
    private lateinit var organizationEmail : EditText
    private lateinit var organizationPhone: EditText
    private lateinit var organizationWebsite: EditText
    private lateinit var organizationIndustry: EditText
    private lateinit var organizationType: Spinner
    private lateinit var organizationSize: Spinner
    private lateinit var organizationTagline: EditText
    private lateinit var organizationDescription: EditText
    private lateinit var organizationLogo: ImageView
    private lateinit var submitButton: Button
    private lateinit var organizationItem: OrganizationData
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

        initializeViews()
        organizationItem = OrganizationData()
        organizationItem.organizationName= organizationNameEditText.text.toString()
        organizationItem.organizationLocation = organizationLocation.text.toString()
        organizationItem.organizationEmail= organizationEmail.text.toString()
        organizationItem.organizationPhone = organizationPhone.text.toString()
        organizationItem.organizationWebsite = organizationWebsite.text.toString()
        organizationItem.organizationIndustry = organizationIndustry.text.toString()
        organizationItem.organizationType = organizationType.selectedItem.toString()
        organizationItem.organizationSize = organizationSize.selectedItem.toString()
        organizationItem.organizationTagline = organizationTagline.text.toString()
        organizationItem.organizationDescription = organizationDescription.text.toString()
        organizationItem.organizationLogo = organizationLogo.toString()

        organizationLogo.setOnClickListener { selectImage() }

        // button listeners initialization
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            // Create organization
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
           val imageUri = data.data
            organizationLogo.setImageURI(imageUri)
            organizationLogo.clipToOutline = true
        }
    }
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    fun initializeViews(){
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
        organizationLogo = findViewById(R.id.organizationLogoImageButton)
        submitButton = findViewById(R.id.submitButton)
        // Populate spinners
        populateSpinners()
    }

    private fun populateSpinners() {
        // Set adapter for Organization Type
        ArrayAdapter.createFromResource(
            this,
            R.array.organization_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            organizationType.adapter = adapter
        }

        // Set adapter for Organization Size
        ArrayAdapter.createFromResource(
            this,
            R.array.organization_sizes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            organizationSize.adapter = adapter
        }
    }
}