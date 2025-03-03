package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.editPage

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData

class editPageFragment : Fragment() {

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
    private lateinit var updateButton: Button

    private val viewModel: EditPageViewModel by viewModels()

    companion object {
        fun newInstance() = editPageFragment()
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)


        // Set up image selection for logo
        organizationLogo.setOnClickListener { selectImage() }

        // Set up submit button
        updateButton.setOnClickListener {
            val name = organizationNameEditText.text.toString()
            val location = organizationLocation.text.toString()
            val email = organizationEmail.text.toString()
            val phone = organizationPhone.text.toString()
            val website = organizationWebsite.text.toString()
            val industry = organizationIndustry.text.toString()
            val type = organizationType.selectedItem.toString()
            val size = organizationSize.selectedItem.toString()
            val tagline = organizationTagline.text.toString()
            val description = organizationDescription.text.toString()

            // Get logo URI (optional, assuming it's set somewhere)
            val logoUri = organizationLogo.tag as? String ?: ""

            // Validate input before submitting
            if (name.isBlank() || email.isBlank() || phone.isBlank()) {
                Toast.makeText(requireContext(), "Name, Email, and Phone are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create organization object
            val updatedData = OrganizationData(
               organizationName = name,
                organizationLocation = location,
                organizationEmail = email,
                organizationPhone = phone,
                organizationWebsite = website,
                organizationIndustry = industry,
                organizationType = type,
                organizationSize = size,
                organizationTagline = tagline,
                organizationDescription = description,
                organizationLogo = logoUri // Store logo URL
            )
            val orgId = "organizationId" // Replace with actual organization ID
            // Call ViewModel to update data
            viewModel.updateOrganization(orgId, updatedData)
        }
    }


    private fun initializeViews(view: View) {
        organizationNameEditText = view.findViewById(R.id.organizationNameEditText)
        organizationLocation = view.findViewById(R.id.organizationLocationEditText)
        organizationEmail = view.findViewById(R.id.organizationEmailEditText)
        organizationIndustry = view.findViewById(R.id.organizationIndustryEditText)
        organizationWebsite = view.findViewById(R.id.organizationWebsiteEditText)
        organizationPhone = view.findViewById(R.id.organizationPhoneEditText)
        organizationType = view.findViewById(R.id.organizationTypeSpinner)
        organizationSize = view.findViewById(R.id.organizationSizeSpinner)
        organizationTagline = view.findViewById(R.id.organizationTaglineEditText)
        organizationDescription = view.findViewById(R.id.organizationDescriptionEditText)
        organizationLogo = view.findViewById(R.id.organizationLogoImageView)
        updateButton = view.findViewById(R.id.updateButton)

        // Populate spinners
        populateSpinners()
    }

    private fun setupListeners() {
        // Handle logo selection
        organizationLogo.setOnClickListener {
            selectImage()
        }

    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("Use registerForActivityResult() instead for better compatibility.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val imageUri = data.data
            organizationLogo.setImageURI(imageUri)
            organizationLogo.clipToOutline = true
        }
    }

    private fun populateSpinners() {
        context?.let {
            ArrayAdapter.createFromResource(
                it, R.array.organization_types, android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                organizationType.adapter = adapter
            }

            ArrayAdapter.createFromResource(
                it, R.array.organization_sizes, android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                organizationSize.adapter = adapter
            }
        }
    }
}
