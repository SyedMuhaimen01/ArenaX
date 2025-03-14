package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.editPage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.*
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

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
    private lateinit var requestQueue: RequestQueue
    private var organizationName: String = ""
    private var originalData: JSONObject = JSONObject()
    private var selectedImageUri: Uri? = null

    companion object {
        fun newInstance(organizationName: String): editPageFragment {
            return editPageFragment().apply {
                arguments = Bundle().apply {
                    putString("organization_name", organizationName)
                }
            }
        }

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
        requestQueue = Volley.newRequestQueue(requireContext())
        populateSpinners()

        organizationName = arguments?.getString("organization_name") ?: ""
        fetchOrganizationData(organizationName)

        organizationLogo.setOnClickListener { selectImage() }
        updateButton.setOnClickListener { updateOrganizationData() }
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

        populateSpinners()
    }

    private fun fetchOrganizationData(orgName: String) {
        val url = "${Constants.SERVER_URL}registerOrganization/organizationDetails"

        val requestBody = JSONObject().apply {
            put("organizationName", orgName)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                originalData = response
                fillOrganizationData(response)
            },
            { error ->
                Log.e("VolleyError", "Error fetching organization data: ${error.message}")
                Toast.makeText(requireContext(), "Failed to fetch organization data", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(request)
    }

    private fun fillOrganizationData(response: JSONObject) {
        organizationNameEditText.setText(response.optString("organization_name", ""))
        organizationLocation.setText(response.optString("organization_location", ""))
        organizationEmail.setText(response.optString("organization_email", ""))
        organizationPhone.setText(response.optString("organization_phone", ""))
        organizationWebsite.setText(response.optString("organization_website", ""))
        organizationIndustry.setText(response.optString("organization_industry", ""))
        organizationTagline.setText(response.optString("organization_tagline", ""))
        organizationDescription.setText(response.optString("organization_description", ""))

        view?.post {
            setSpinnerValue(organizationType, response.optString("organization_type", ""))
            setSpinnerValue(organizationSize, response.optString("organization_size", ""))
        }

        val logoUrl = response.optString("organization_logo", "").takeIf { it.isNotBlank() }
        if (!logoUrl.isNullOrEmpty()) {
            Glide.with(this).load(logoUrl).into(organizationLogo)
            organizationLogo.tag = logoUrl
        } else {
            Glide.with(this).clear(organizationLogo)
            organizationLogo.setImageResource(R.drawable.add_icon_foreground)
            organizationLogo.tag = ""
        }
    }

    private fun updateOrganizationData() {
        val updatedData = JSONObject()
        updatedData.put("organizationName", organizationName)
        updatedData.put("organizationLocation", getTextOrDefault(organizationLocation, "organization_location"))
        updatedData.put("organizationEmail", getTextOrDefault(organizationEmail, "organization_email"))
        updatedData.put("organizationPhone", getTextOrDefault(organizationPhone, "organization_phone"))
        updatedData.put("organizationWebsite", getTextOrDefault(organizationWebsite, "organization_website"))
        updatedData.put("organizationIndustry", getTextOrDefault(organizationIndustry, "organization_industry"))
        updatedData.put("organizationTagline", getTextOrDefault(organizationTagline, "organization_tagline"))
        updatedData.put("organizationDescription", getTextOrDefault(organizationDescription, "organization_description"))
        updatedData.put("organizationType", getSpinnerOrDefault(organizationType, "organization_type"))
        updatedData.put("organizationSize", getSpinnerOrDefault(organizationSize, "organization_size"))

        val existingLogoUrl = organizationLogo.tag as? String ?: ""
        updatedData.put("organizationLogo", selectedImageUri?.toString() ?: existingLogoUrl)

        val request = JsonObjectRequest(
            Request.Method.POST, "${Constants.SERVER_URL}registerOrganization/update", updatedData,
            { response ->
                Toast.makeText(requireContext(), "Organization Updated Successfully", Toast.LENGTH_SHORT).show()
                Log.d("UpdateSuccess", "Response: $response")
            },
            { error ->
                Log.e("VolleyError", "Failed to update organization: ${error.networkResponse?.statusCode} - ${error.message}")
                Toast.makeText(requireContext(), "Failed to update organization details", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(request)
    }

    private fun getTextOrDefault(editText: EditText, key: String): String {
        return editText.text.toString().trim().ifEmpty { originalData.optString(key, "") }
    }

    private fun getSpinnerOrDefault(spinner: Spinner, key: String): String {
        return spinner.selectedItem?.toString()?.trim()?.ifEmpty { originalData.optString(key, "") } ?: originalData.optString(key, "")
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Glide.with(this).load(selectedImageUri).into(organizationLogo)
        }
    }

    private fun populateSpinners() {
        context?.let { ctx ->
            ArrayAdapter.createFromResource(ctx, R.array.organization_types, android.R.layout.simple_spinner_item).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                organizationType.adapter = adapter
            }

            ArrayAdapter.createFromResource(ctx, R.array.organization_sizes, android.R.layout.simple_spinner_item).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                organizationSize.adapter = adapter
            }
        }
    }

    private fun setSpinnerValue(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        if (adapter is ArrayAdapter<*>) {
            val position = (adapter as ArrayAdapter<String>).getPosition(value)
            if (position >= 0) {
                spinner.setSelection(position)
            }
        } else {
            Log.e("EditPageFragment", "Spinner adapter is null or not an ArrayAdapter")
        }
    }
}
