package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.muhaimen.arenax.R


class upcommingEvents : Fragment() {
    private lateinit var upcomingEventsAdapter: upcomingEventsAdapter
    private lateinit var upcomingEventsRecyclerView: RecyclerView
    private lateinit var searchEventsRecyclerView: RecyclerView
    private lateinit var searchEventsAdapter: searchEventsAdapter
    private lateinit var searchBar: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_upcomming_events, container, false)

        upcomingEventsRecyclerView = view.findViewById(R.id.upcomingEventsRecyclerview)
        searchEventsRecyclerView = view.findViewById(R.id.searchEventsRecyclerView)
        searchBar = view.findViewById(R.id.searchbar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        searchEventsAdapter = searchEventsAdapter(emptyList())
        searchEventsRecyclerView.adapter = searchEventsAdapter
        searchEventsRecyclerView.layoutManager = LinearLayoutManager(context)
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                upcomingEventsRecyclerView.visibility = View.GONE
                searchEventsRecyclerView.visibility = View.VISIBLE
            }
        }
        upcomingEventsRecyclerView.layoutManager = LinearLayoutManager(context)
        upcomingEventsAdapter = upcomingEventsAdapter(emptyList())
        return view
    }

}