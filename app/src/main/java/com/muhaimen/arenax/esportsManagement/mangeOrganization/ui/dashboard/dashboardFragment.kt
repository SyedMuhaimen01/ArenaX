package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

class dashboardFragment : Fragment() {

    private var organizationNameTextView: TextView? = null
    private var organizationLocationTextView: TextView? = null
    private var organizationEmailTextView: TextView? = null
    private var organizationPhoneTextView: TextView? = null
    private var organizationWebsiteTextView: TextView? = null
    private var organizationIndustryTextView: TextView? = null
    private var organizationTypeTextView: TextView? = null
    private var organizationSizeTextView: TextView? = null
    private var organizationTaglineTextView: TextView? = null
    private var organizationDescriptionTextView: TextView? = null
    private var organizationLogoImageView: ImageView? = null

    private lateinit var requestQueue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        initializeViews(view)

        requestQueue = Volley.newRequestQueue(requireContext())

        // Fetch organization name from arguments
        val organizationName = arguments?.getString("organization_name")
        Log.d("DashboardFragment", "Organization name: $organizationName")

        if (!organizationName.isNullOrEmpty()) {
            fetchOrganizationDetails(organizationName)
        } else {
            //Toast.makeText(requireContext(), "Organization name is missing", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun initializeViews(view: View) {
        organizationNameTextView = view.findViewById(R.id.organizationNameTextView)
        organizationLocationTextView = view.findViewById(R.id.organizationLocationTextView)
        organizationEmailTextView = view.findViewById(R.id.organizationEmailTextView)
        organizationIndustryTextView = view.findViewById(R.id.organizationIndustryTextView)
        organizationWebsiteTextView = view.findViewById(R.id.organizationWebsiteTextView)
        organizationPhoneTextView = view.findViewById(R.id.organizationPhoneTextView)
        organizationTypeTextView = view.findViewById(R.id.organizationTypeTextView)
        organizationSizeTextView = view.findViewById(R.id.organizationSizeTextView)
        organizationTaglineTextView = view.findViewById(R.id.organizationTaglineTextView)
        organizationDescriptionTextView = view.findViewById(R.id.organizationDescriptionTextView)
        organizationLogoImageView = view.findViewById(R.id.organizationLogoImageView)
    }

    private fun fetchOrganizationDetails(orgName: String) {
        Log.d("DashboardFragment", "Fetching organization details for: $orgName")
        val url = "${Constants.SERVER_URL}registerOrganization/organizationDetails"

        val requestBody = JSONObject().apply {
            put("organizationName", orgName)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    // Check if view is available before modifying it
                    organizationNameTextView?.text = response.optString("organization_name", "N/A")
                    organizationLocationTextView?.text = response.optString("organization_location", "N/A")
                    organizationEmailTextView?.text = response.optString("organization_email", "N/A")
                    organizationPhoneTextView?.text = response.optString("organization_phone", "N/A")
                    organizationWebsiteTextView?.text = response.optString("organization_website", "N/A")
                    organizationIndustryTextView?.text = response.optString("organization_industry", "N/A")
                    organizationTypeTextView?.text = response.optString("organization_type", "N/A")
                    organizationSizeTextView?.text = response.optString("organization_size", "N/A")
                    organizationTaglineTextView?.text = response.optString("organization_tagline", "N/A")
                    organizationDescriptionTextView?.text = response.optString("organization_description", "N/A")

                    // Load organization logo using Glide
                    val logoUrl =
                        "https://firebasestorage.googleapis.com/v0/b/arenax-e1289.appspot.com/o/profileImages%2FPV5rfHtJqkcQvWMy5SYhQzEVEuK2%2Fprofile.jpg?alt=media&token=d010b921-fb9d-4a93-a7ed-94648ff081e9"

                    organizationLogoImageView?.let {
                        Glide.with(requireContext())
                            .load(logoUrl)
                            .placeholder(R.drawable.add_icon_foreground)
                            .into(it)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Failed to fetch organization details", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(jsonObjectRequest)
    }
}
