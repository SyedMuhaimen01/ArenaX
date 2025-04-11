package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageEmployees

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONObject

class manageEmployees : AppCompatActivity() {
    private lateinit var employeesRecyclerView: RecyclerView
    private lateinit var manageEmployeesAdapter: ManageEmployeesAdapter
    private lateinit var searchEmployeesAdapter: SearchEmployeesAdapter
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var backButton: ImageButton
    private lateinit var addEmployeeButton: FloatingActionButton
    private lateinit var searchbarLinearLayout: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var organizationName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_employess)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        organizationName = intent.getStringExtra("organization_name") ?: ""
        database = FirebaseDatabase.getInstance().reference.child("userData")

        fetchEmployeesList()
        employeesRecyclerView = findViewById(R.id.employeesRecyclerview)
        searchRecyclerView = findViewById(R.id.searchEmployeesRecyclerView)
        searchBar = findViewById(R.id.searchbar)
        searchbarLinearLayout = findViewById(R.id.searchbarLinearLayout)
        searchRecyclerView.layoutManager = LinearLayoutManager(this)

        searchEmployeesAdapter = SearchEmployeesAdapter(mutableListOf()) { employeeId ->
            addEmployee(employeeId)
        }
        searchRecyclerView.adapter = searchEmployeesAdapter

        employeesRecyclerView.layoutManager = LinearLayoutManager(this)
        manageEmployeesAdapter = ManageEmployeesAdapter(mutableListOf()) { employeeId ->
            removeEmployee(employeeId)
        }
        employeesRecyclerView.adapter = manageEmployeesAdapter

        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                employeesRecyclerView.visibility = View.GONE
                searchRecyclerView.visibility = VISIBLE
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.trim() ?: ""
                if (searchText.isNotEmpty()) {
                    searchUsers(searchText)
                } else {
                    searchEmployeesAdapter.updateEmployeesList(emptyList())
                }
            }
        })

        addEmployeeButton = findViewById(R.id.addEmployeeButton)
        addEmployeeButton.setOnClickListener {
            if (searchbarLinearLayout.visibility == View.VISIBLE) {
                searchbarLinearLayout.visibility = View.GONE
                searchRecyclerView.visibility = GONE
                employeesRecyclerView.visibility = View.VISIBLE
            } else {
                searchbarLinearLayout.visibility = View.VISIBLE
                searchRecyclerView.visibility = VISIBLE
                employeesRecyclerView.visibility = View.GONE
            }
        }

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun searchUsers(query: String) {
        val usersList = mutableListOf<UserData>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserData::class.java)
                    if (user != null && user.userId != FirebaseManager.getCurrentUserId()) {
                        if (user.fullname.contains(query, ignoreCase = true) ||
                            user.gamerTag.contains(query, ignoreCase = true)
                        ) {
                            if (!usersList.any { it.userId == user.userId }) {
                                usersList.add(user)
                            }
                        }
                    }
                }
                searchEmployeesAdapter.updateEmployeesList(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ManageAdmins", "Search error: ${error.message}")
            }
        })
    }

    private fun fetchEmployeesList() {
        val url = "${Constants.SERVER_URL}manageOrganizations/getEmployees"
        val requestBody = JSONObject().apply {
            put("organizationName", organizationName)
        }

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonArray = JSONObject(response).getJSONArray("employees")
                    val employeesList = mutableListOf<UserData>()

                    for (i in 0 until jsonArray.length()) {
                        val userJson = jsonArray.getJSONObject(i)
                        val user = UserData(
                            userId = userJson.getString("userId"),
                            fullname = userJson.getString("fullname"),
                            gamerTag = userJson.optString("gamerTag", ""),
                            profilePicture = userJson.optString("profilePicture", null)
                        )
                        employeesList.add(user)
                    }

                    manageEmployeesAdapter.updateEmployeesList(employeesList)
                } catch (e: Exception) {
                    Log.e("FetchEmployees", "Error parsing response: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("FetchEmployees", "Error fetching employees: ${error.message}")
            }
        ) {
            override fun getBody() = requestBody.toString().toByteArray(Charsets.UTF_8)
            override fun getHeaders() = mutableMapOf("Content-Type" to "application/json")
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun addEmployee(userId: String) {
        val context: Context = this
        val currentUser = FirebaseManager.getCurrentUserId()
        Log.d("AddEmployee", "Adding employee: $userId, current user: $currentUser")
        val url = "${Constants.SERVER_URL}manageOrganizations/addEmployee"

        val requestBody = JSONObject().apply {
            put("organizationName", organizationName)
            put("currentUserId", currentUser)
            put("employeeId", userId)
        }

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                Log.d("AddEmployee", "Employee added successfully")
                Toast.makeText(context, "Employee added successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, manageEmployees::class.java)
                intent.putExtra("organization_name", organizationName)
                startActivity(intent)
            },
            Response.ErrorListener { error ->
                error.networkResponse?.let { response ->
                    val responseData = String(response.data, Charsets.UTF_8)
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val errorMessage = jsonResponse.optString("error", "An unknown error occurred")

                        when (errorMessage) {
                            "You do not have the authority to add an employee" ->
                                Toast.makeText(context, "You don't have permission to add an employee", Toast.LENGTH_LONG).show()
                            "User is already an employee" ->
                                Toast.makeText(context, "This user is already an employee", Toast.LENGTH_LONG).show()
                            "User is already an admin and cannot be added as an employee" ->
                                Toast.makeText(context, "This user is an admin and cannot be added as an employee", Toast.LENGTH_LONG).show()
                            "Current user not found" ->
                                Toast.makeText(context, "Your account is not recognized. Please log in again.", Toast.LENGTH_LONG).show()
                            "Employee user not found" ->
                                Toast.makeText(context, "The selected user does not exist.", Toast.LENGTH_LONG).show()
                            "Organization not found" ->
                                Toast.makeText(context, "Organization not found. Please check the name.", Toast.LENGTH_LONG).show()
                            else ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }

                        Log.e("AddEmployee", "Error: $errorMessage")
                    } catch (e: Exception) {
                        Log.e("AddEmployee", "Error parsing response: ${e.message}")
                        Toast.makeText(context, "Failed to add employee. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    Log.e("AddEmployee", "Unknown error: ${error.message}")
                    Toast.makeText(context, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        ) {
            override fun getBody() = requestBody.toString().toByteArray(Charsets.UTF_8)
            override fun getHeaders() = mutableMapOf("Content-Type" to "application/json")
        }

        Volley.newRequestQueue(context).add(request)

    }

    private fun removeEmployee(userId: String) {
        val context: Context = this
        val url = "${Constants.SERVER_URL}manageOrganizations/removeEmployee"
        val currentUser = FirebaseManager.getCurrentUserId()

        val requestBody = JSONObject().apply {
            put("organizationName", organizationName)
            put("currentUserId", currentUser) // Current user making the request
            put("employeeId", userId) // Employee to be removed
        }

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                Log.d("RemoveEmployee", "Employee removed successfully")
                Toast.makeText(context, "Employee removed successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, manageEmployees::class.java) // Redirect to Manage Employees screen
                intent.putExtra("organization_name", organizationName)
                startActivity(intent)
            },
            Response.ErrorListener { error ->
                error.networkResponse?.let { response ->
                    val responseData = String(response.data, Charsets.UTF_8)
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val errorMessage = jsonResponse.optString("error", "An unknown error occurred")

                        if (errorMessage == "You do not have the authority to remove an employee") {
                            Toast.makeText(context, "You don't have permission to remove an employee", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }

                        Log.e("RemoveEmployee", "Error: $errorMessage")
                    } catch (e: Exception) {
                        Log.e("RemoveEmployee", "Error parsing response: ${e.message}")
                        Toast.makeText(context, "Failed to remove employee. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    Log.e("RemoveEmployee", "Unknown error: ${error.message}")
                    Toast.makeText(context, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        ) {
            override fun getBody() = requestBody.toString().toByteArray(Charsets.UTF_8)
            override fun getHeaders() = mutableMapOf("Content-Type" to "application/json")
        }

        Volley.newRequestQueue(context).add(request)
    }

}