package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.jobPosting

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Job

class jobPosting : AppCompatActivity() {
    private lateinit var jobTitleEditText: EditText
    private lateinit var jobType: Spinner
    private lateinit var jobLocation: EditText
    private lateinit var jobDescription: EditText
    private lateinit var workplaceType: Spinner
    private lateinit var jobTag1: EditText
    private lateinit var jobTag2: EditText
    private lateinit var jobTag3: EditText
    private lateinit var jobTag4: EditText
    private lateinit var postJobButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var jobItem: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_posting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        initializeViews()
        jobItem = Job("", "", "", "", "", "", "", listOf())
        jobItem.jobTitle = jobTitleEditText.text.toString()
        jobItem.jobType = jobType.selectedItem.toString()
        jobItem.jobLocation = jobLocation.text.toString()
        jobItem.jobDescription = jobDescription.text.toString()
        jobItem.workplaceType = workplaceType.selectedItem.toString()
        val tag1 = jobTag1.text.toString()
        val tag2 = jobTag2.text.toString()
        val tag3 = jobTag3.text.toString()
        val tag4 = jobTag4.text.toString()

        jobItem.tags = listOf(tag1, tag2, tag3, tag4)

        postJobButton.setOnClickListener {
            // Post job to database
        }
        backButton.setOnClickListener {
            onBackPressed()
            finish()
        }
    }

    fun initializeViews() {
        jobTitleEditText = findViewById(R.id.jobTitleEditText)
        jobType = findViewById(R.id.jobTypeSpinner)
        jobLocation = findViewById(R.id.jobLocationEditText)
        jobDescription = findViewById(R.id.jobDescriptionEditText)
        workplaceType = findViewById(R.id.workplaceTypeSpinner)
        jobTag1 = findViewById(R.id.tag1)
        jobTag2 = findViewById(R.id.tag2)
        jobTag3 = findViewById(R.id.tag3)
        jobTag4 = findViewById(R.id.tag4)
        postJobButton = findViewById(R.id.postJobButton)
        backButton = findViewById(R.id.backButton)

        populateSpinners()
    }
    private fun populateSpinners() {
        // Set adapter for Organization Type
        ArrayAdapter.createFromResource(
            this,
            R.array.job_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            jobType.adapter = adapter
        }

        // Set adapter for Organization Size
        ArrayAdapter.createFromResource(
            this,
            R.array.workplace_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            workplaceType.adapter = adapter
        }
    }
}