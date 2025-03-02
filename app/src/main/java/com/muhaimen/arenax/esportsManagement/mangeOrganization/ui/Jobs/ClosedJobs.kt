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


class ClosedJobs : Fragment() {

    private lateinit var closedJobsRecyclerView: RecyclerView
    private lateinit var closedJobsAdapter: ClosedJobsAdapter
    private lateinit var searchJobRecyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_closed_jobs, container, false)

        // Initialize UI elements
        closedJobsRecyclerView = view.findViewById(R.id.closedJobs_recyclerview)
        searchJobRecyclerView = view.findViewById(R.id.searchJobRecyclerView)
        searchBar = view.findViewById(R.id.searchbar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        val searchJobAdapter =SearchJobAdapter(emptyList())
        searchJobRecyclerView.layoutManager = LinearLayoutManager(context)
        searchJobRecyclerView.adapter = searchJobAdapter
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                closedJobsRecyclerView.visibility = View.GONE
                searchJobRecyclerView.visibility = View.VISIBLE
            }
        }
        closedJobsRecyclerView.layoutManager = LinearLayoutManager(context)
        closedJobsAdapter = ClosedJobsAdapter(emptyList())
        return view
    }
}
