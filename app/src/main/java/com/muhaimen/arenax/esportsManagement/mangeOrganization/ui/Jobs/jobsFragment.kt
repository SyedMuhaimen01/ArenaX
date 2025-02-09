package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.jobPosting.jobPosting

class jobsFragment : Fragment() {

    private lateinit var postJobButton:FloatingActionButton
    companion object {
        fun newInstance() = jobsFragment()
    }

    private val viewModel: JobsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postJobButton = view.findViewById(R.id.postButton)
        postJobButton.setOnClickListener {
            val intent=Intent(context, jobPosting::class.java)
            startActivity(intent)
        }
    }
}