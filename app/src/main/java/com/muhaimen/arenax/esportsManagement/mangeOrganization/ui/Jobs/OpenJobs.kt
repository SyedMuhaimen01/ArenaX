package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.muhaimen.arenax.R

class OpenJobs : Fragment() {

    private lateinit var openJobsRecyclerView: RecyclerView
    private lateinit var openJobsAdapter: OpenJobsAdapter
    private lateinit var searchJobRecyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_open_jobs, container, false)

        // Initialize UI elements
        openJobsRecyclerView = view.findViewById(R.id.openJobs_recyclerview)
        searchJobRecyclerView = view.findViewById(R.id.searchJobRecyclerView)
        searchBar = view.findViewById(R.id.searchbar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        val searchJobAdapter =SearchJobAdapter(emptyList())
        searchJobRecyclerView.layoutManager = LinearLayoutManager(context)
        searchJobRecyclerView.adapter = searchJobAdapter
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                openJobsRecyclerView.visibility = View.GONE
                searchJobRecyclerView.visibility = View.VISIBLE
            }
        }

        openJobsRecyclerView.layoutManager = LinearLayoutManager(context)
        openJobsAdapter = OpenJobsAdapter(emptyList())

        return view
    }
}
