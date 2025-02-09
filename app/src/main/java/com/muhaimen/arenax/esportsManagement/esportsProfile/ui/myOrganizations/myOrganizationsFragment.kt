package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myOrganizations

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.mangeOrganization.createOrganization.createOrganization

class myOrganizationsFragment : Fragment() {

    private lateinit var createOrganizationButton: Button
    companion object {
        fun newInstance() = myOrganizationsFragment()
    }

    private val viewModel: MyOrganizationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_organizations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createOrganizationButton = view.findViewById(R.id.createOrganizationButton)
        createOrganizationButton.setOnClickListener {
            val intent= Intent(activity, createOrganization::class.java)
            startActivity(intent)
        }
    }
}