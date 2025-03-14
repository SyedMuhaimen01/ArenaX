package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins.manageAdmins
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageEmployees.manageEmployees
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageFollowing.manageFollowing
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

class settingsFragment : Fragment() {
    private lateinit var manageAdminsButton: LinearLayout
    private lateinit var manageEmployeesButton: LinearLayout
    private lateinit var deleteOrganizationButton: LinearLayout
    private lateinit var manageFollowingButton: LinearLayout
    private lateinit var database:FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    private val viewModel: SettingsViewModel by viewModels()

    // Variable to store organization name
    private var organizationName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve organization name from arguments
        organizationName = arguments?.getString("organization_name")
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

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        manageAdminsButton.setOnClickListener {
            val intent = Intent(context, manageAdmins::class.java)
            intent.putExtra("organization_name", organizationName)
            startActivity(intent)
        }

        manageEmployeesButton.setOnClickListener {
            val intent = Intent(context, manageEmployees::class.java)
            intent.putExtra("organization_name", organizationName)
            startActivity(intent)
        }

        manageFollowingButton.setOnClickListener {
            val intent = Intent(context, manageFollowing::class.java)
            intent.putExtra("organization_name", organizationName)
            startActivity(intent)
        }

        val builder = context?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        builder?.setTitle("Delete Organization")
        builder?.setMessage("Are you sure you want to delete this organization?")
        builder?.setPositiveButton("Yes") { _, _ ->
            deleteOrganization(organizationName, userId)
        }
        builder?.setNegativeButton("No") { _, _ ->
            // Do nothing
        }
        deleteOrganizationButton.setOnClickListener {
            builder?.show()
        }
    }

    private fun deleteOrganization(organizationName: String?, firebaseUid: String) {
        if (organizationName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Organization name is missing!", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonBody = JSONObject().apply {
            put("organization_name", organizationName)
            put("firebaseUid", firebaseUid)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Constants.SERVER_URL}registerOrganization/deleteOrganization",
            jsonBody,
            { response ->
                Toast.makeText(requireContext(), "Organization deleted successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to TalentExchange Activity
                val intent = Intent(requireContext(), talentExchange::class.java)
                startActivity(intent)

                // Finish current activity if needed
                requireActivity().finish()
            },
            { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to delete organization: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

}
