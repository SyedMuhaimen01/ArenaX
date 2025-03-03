package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.dashboard

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.muhaimen.arenax.R

class dashboardFragment : Fragment() {

    private lateinit var organizationNameEditText: TextView
    private lateinit var organizationLocation: TextView
    private lateinit var organizationEmail: TextView
    private lateinit var organizationPhone: TextView
    private lateinit var organizationWebsite: TextView
    private lateinit var organizationIndustry: TextView
    private lateinit var organizationType: TextView
    private lateinit var organizationSize: TextView
    private lateinit var organizationTagline: TextView
    private lateinit var organizationDescription: TextView
    private lateinit var postsCount: TextView
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private lateinit var organizationLogo: ImageView

    companion object {
        fun newInstance() = dashboardFragment()
    }

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    private fun initializeViews(view: View) {

        organizationNameEditText = view.findViewById(R.id.organizationNameTextView)
        organizationLocation = view.findViewById(R.id.organizationLocationTextView)
        organizationEmail = view.findViewById(R.id.organizationEmailTextView)
        organizationIndustry = view.findViewById(R.id.organizationIndustryTextView)
        organizationWebsite = view.findViewById(R.id.organizationWebsiteTextView)
        organizationPhone = view.findViewById(R.id.organizationPhoneTextView)
        organizationType = view.findViewById(R.id.organizationTypeTextView)
        organizationSize = view.findViewById(R.id.organizationSizeTextView)
        organizationTagline = view.findViewById(R.id.organizationTaglineTextView)
        organizationDescription = view.findViewById(R.id.organizationDescriptionTextView)
        organizationLogo = view.findViewById(R.id.organizationLogoImageView)
        postsCount = view.findViewById(R.id.postsCountTextView)
        followersCount = view.findViewById(R.id.followersCountTextView)
        followingCount = view.findViewById(R.id.followingCountTextView)
    }
}