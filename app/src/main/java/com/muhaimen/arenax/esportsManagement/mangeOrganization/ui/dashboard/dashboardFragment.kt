package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    private var followersCountTextView: TextView? = null
    private var followingCountTextView: TextView? = null
    private var postCountTextView: TextView? = null

    private lateinit var requestQueue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        initializeViews(view)

        requestQueue = Volley.newRequestQueue(requireContext())

        val organizationName = arguments?.getString("organization_name")
        Log.d("DashboardFragment", "Organization name: $organizationName")

        if (!organizationName.isNullOrEmpty()) {
            fetchOrganizationDetails(organizationName)
            getOrganizationPostCount(organizationName)
            getOrganizationDetails(organizationName)
        }

        organizationEmailTextView?.setOnClickListener {
            val emailAddress =
                organizationEmailTextView?.text

            // Create an intent to send an email
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))

            }
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
            }

            organizationPhoneTextView?.setOnClickListener {
                val phoneNumber = organizationPhoneTextView!!.text

                // Create an intent to open the dialer
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }

                // Check if there is an app available to handle the intent
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    Log.d("DashboardFragment", "No app available to handle the intent")
                }
            }
        }
            return view
    }
    private fun getOrganizationDetails(organizationName: String) {
        // Reference to the organizationsData node in Firebase
        val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")

        // Query to find the organization by its name
        val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(organizationName)

        // Execute the query to find the organization
        orgQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate through the results (though there should only be one match)
                    for (orgSnapshot in snapshot.children) {
                        // Retrieve the organization ID
                        val orgId = orgSnapshot.key

                        // Fetch notifications for this organization
                        if (!orgId.isNullOrEmpty()) {
                            fetchAndSetCounts(orgId)

                        } else {
                            println("Organization ID is null or empty")
                        }
                    }
                } else {
                    // Handle the case where no organization is found with the given name
                    println("No organization found with the name: $organizationName")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors when fetching data
                println("Database error: ${error.message}")
            }
        })
    }
    private fun fetchAndSetCounts(orgId: String) {
        val followersRef = FirebaseDatabase.getInstance().getReference("organizationsData/$orgId/synerG/followers")
        val followingRef = FirebaseDatabase.getInstance().getReference("organizationsData/$orgId/synerG/following")


        // Fetch and count followers with status "accepted"
        followersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val acceptedFollowersCount = snapshot.children.count {
                    it.child("status").value?.toString() == "accepted"
                }
                followersCountTextView?.text = acceptedFollowersCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Error fetching followers: ${error.message}")
            }
        })

        // Fetch and count following with status "accepted"
        followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val acceptedFollowingCount = snapshot.children.count {
                    it.child("status").value?.toString() == "accepted"
                }
                followingCountTextView?.text   = acceptedFollowingCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Error fetching following: ${error.message}")
            }
        })
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
        organizationLogoImageView = view.findViewById(R.id.profilePicture)

        followersCountTextView = view.findViewById(R.id.followersCountTextView)
        followingCountTextView = view.findViewById(R.id.followingCountTextView)
        postCountTextView = view.findViewById(R.id.postsCountTextView)
    }

    @SuppressLint("SetTextI18n")
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

                    followersCountTextView?.text = response.optInt("followers", 0).toString()
                    followingCountTextView?.text = response.optInt("following", 0).toString()

                    val logoUrl = response.optString("organization_logo", "").takeIf { it.isNotBlank() }

                    organizationLogoImageView?.let { imageView ->
                        // Set clipToOutline on the ImageView
                        imageView.clipToOutline = true

                        // Load the image using Glide
                        Glide.with(requireContext())
                            .load(logoUrl ?: R.drawable.add_icon_foreground)
                            .placeholder(R.drawable.add_icon_foreground)
                            .into(imageView)
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

    private fun getOrganizationPostCount(organizationName: String) {
        val url = "${Constants.SERVER_URL}organizationPosts/postCount"

        val jsonBody = JSONObject().apply {
            put("organization_name", organizationName)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            { response ->
                val postCount = response.optInt("postCount", 0)
                postCountTextView?.text = postCount.toString()
            },
            { error ->
                Toast.makeText(context, "Failed to fetch post count: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(request)
    }
}
