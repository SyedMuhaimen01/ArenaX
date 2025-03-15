package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageEmployees

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager

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

        database = FirebaseDatabase.getInstance().reference.child("userData")

        employeesRecyclerView = findViewById(R.id.employeesRecyclerview)
        searchRecyclerView = findViewById(R.id.searchEmployeesRecyclerView)
        searchBar = findViewById(R.id.searchbar)
        searchbarLinearLayout = findViewById(R.id.searchbarLinearLayout)
        // Initialize searchAdminsAdapter
        searchEmployeesAdapter = SearchEmployeesAdapter(mutableListOf()) { employeeId ->
            // Remove admin from organization logic here
        }
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchEmployeesAdapter

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

        employeesRecyclerView.layoutManager = LinearLayoutManager(this)
        manageEmployeesAdapter = ManageEmployeesAdapter(mutableListOf()) { employeeId ->
            // Remove admin from organization logic here
        }
        employeesRecyclerView.adapter = manageEmployeesAdapter

        addEmployeeButton=findViewById(R.id.addEmployeeButton)
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
            finish()
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
}