package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myOrganizations

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.createOrganization.createOrganization
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class myOrganizationsFragment : Fragment() {

    private lateinit var createOrganizationButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyOrganizationsAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId:String
    private val organizationList = mutableListOf<OrganizationData>()

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
        recyclerView = view.findViewById(R.id.organizationRecyclerView)

        database= FirebaseDatabase.getInstance()
        auth= FirebaseAuth.getInstance()
        userId=FirebaseManager.getCurrentUserId().toString()
        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = MyOrganizationsAdapter(organizationList)
        recyclerView.adapter = adapter

        createOrganizationButton.setOnClickListener {
            val intent = Intent(activity, createOrganization::class.java)
            startActivity(intent)
        }

        // Fetch organizations from backend
        fetchOrganizations()
    }

    private fun fetchOrganizations() {
        val url = "${Constants.SERVER_URL}registerOrganization/user/organizations"
        val requestQueue = Volley.newRequestQueue(requireContext())

        // Create JSON body
        val requestBody = JSONObject().apply {
            put("firebaseUid", userId) // Send firebaseUid in the body
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response: JSONObject ->
                organizationList.clear() // Clear previous data

                val organizationsArray = response.optJSONArray("organizations") ?: JSONArray()
                for (i in 0 until organizationsArray.length()) {
                    val orgObject = organizationsArray.getJSONObject(i)
                    val organization = OrganizationData(
                        organizationId = orgObject.getString("organization_id"),
                        organizationName = orgObject.getString("organization_name"),
                        organizationLogo = orgObject.optString("organization_logo", null),
                        organizationLocation = orgObject.optString("organization_location", null)
                    )
                    organizationList.add(organization)
                }

                adapter.notifyDataSetChanged() // Refresh RecyclerView
            },
            { error ->
                Toast.makeText(requireContext(), "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}
