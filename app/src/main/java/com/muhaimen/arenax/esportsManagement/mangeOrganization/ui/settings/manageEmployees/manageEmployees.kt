package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageEmployees

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins.ManageAdminsAdapter
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins.SearchAdminsAdapter

class manageEmployees : AppCompatActivity() {
    private lateinit var employeesRecyclerView: RecyclerView
    private lateinit var manageEmployeesAdapter: ManageEmployeesAdapter
    private lateinit var searchEmployeesAdapter: SearchEmployeesAdapter
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var backButton: ImageButton
    private lateinit var addEmployeeButton: FloatingActionButton
    private lateinit var searchbarLinearLayout: LinearLayout
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

        employeesRecyclerView = findViewById(R.id.employeesRecyclerview)
        searchRecyclerView = findViewById(R.id.searchEmployeesRecyclerView)
        searchBar = findViewById(R.id.searchbar)
        searchbarLinearLayout = findViewById(R.id.searchbarLinearLayout)
        // Initialize searchAdminsAdapter
        searchEmployeesAdapter = SearchEmployeesAdapter(emptyList())
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchEmployeesAdapter

        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                employeesRecyclerView.visibility = View.GONE
                searchRecyclerView.visibility = VISIBLE
            }
        }

        employeesRecyclerView.layoutManager = LinearLayoutManager(this)
        manageEmployeesAdapter = ManageEmployeesAdapter(emptyList())
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
}