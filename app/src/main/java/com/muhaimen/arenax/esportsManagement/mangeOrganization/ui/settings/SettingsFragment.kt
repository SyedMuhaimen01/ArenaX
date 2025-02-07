package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins.manageAdmins
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageEmployees.manageEmployees

class settingsFragment : Fragment() {
    private lateinit var manageAdminsButton:LinearLayout
    private lateinit var manageEmployeesButton:LinearLayout
    private lateinit var deleteOrganizationButton:LinearLayout
    private lateinit var manageFollowingButton:LinearLayout
    companion object {
        fun newInstance() = settingsFragment()
    }

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manageAdminsButton = view.findViewById(R.id.manageAdminsLinearLayout)
        manageEmployeesButton = view.findViewById(R.id.manageEmployeesLinearLayout)
        deleteOrganizationButton = view.findViewById(R.id.deleteOrganizationLinearLayout)
        manageFollowingButton = view.findViewById(R.id.manageFollowingLinearLayout)

        manageAdminsButton.setOnClickListener {
            val intent= Intent(context, manageAdmins::class.java)
            startActivity(intent)
        }

        manageEmployeesButton.setOnClickListener {
            val intent= Intent(context, manageEmployees::class.java)
            startActivity(intent)
        }

        val builder= context?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        builder?.setTitle("Delete Organization")
        builder?.setMessage("Are you sure you want to delete this organization?")
        builder?.setPositiveButton("Yes"){ dialogInterface, which ->
            // Delete organization
        }
        builder?.setNegativeButton("No"){ dialogInterface, which ->
            // Do nothing
        }
        deleteOrganizationButton.setOnClickListener {
            builder?.show()
        }

    }
}