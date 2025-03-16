package com.muhaimen.arenax.esportsManagement.exploreEsports

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Gender
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class exploreOrganizations : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: exploreOrganizationsAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var searchUserRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var database: DatabaseReference
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_accounts, container, false)

        recyclerView = view.findViewById(R.id.accounts_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.searchbar)
        searchUserRecyclerView = view.findViewById(R.id.searchUserRecyclerView)
        val searchOrganizationsAdapter = SearchOrganizationsAdapter(emptyList())
        searchUserRecyclerView.layoutManager = LinearLayoutManager(context)
        searchUserRecyclerView.adapter = searchOrganizationsAdapter
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                recyclerView.visibility = View.GONE
                searchUserRecyclerView.visibility = View.VISIBLE
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.trim() ?: ""
                if (searchText.isNotEmpty()) {
                    searchOrganizations(searchText, searchOrganizationsAdapter)
                } else {
                    searchOrganizationsAdapter.updateOrganizationsList(emptyList())
                }
            }
        })

        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchUserRecyclerView.visibility == View.VISIBLE) {
                    searchUserRecyclerView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    searchEditText.text.clear()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })

        // Initialize database reference
        database = FirebaseDatabase.getInstance().getReference("organizationsData")
        // Fetch user profiles


        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primaryColor)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            //fetchUserProfiles()

        }

        return view
    }

    //Not required in current implementation


    private fun searchOrganizations(query: String, adapter: SearchOrganizationsAdapter) {
        val organizationsList = mutableListOf<OrganizationData>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                organizationsList.clear()
                for (organizationSnapshot in snapshot.children) {
                    val organization = organizationSnapshot.getValue(OrganizationData::class.java)

                    // Perform a case-insensitive check on organizationName
                    if (organization?.organizationName?.contains(query, ignoreCase = true) == true) {
                        // Add only unique organizations to the list
                        if (!organizationsList.any { it.organizationId == organization.organizationId }) {
                            organizationsList.add(organization)
                        }
                    }
                }
                adapter.updateOrganizationsList(organizationsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ExploreAccounts", "Search error: ${error.message}")
                Toast.makeText(context, "Error searching organizations: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}


