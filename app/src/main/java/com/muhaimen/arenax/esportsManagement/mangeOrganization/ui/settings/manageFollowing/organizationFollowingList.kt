package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageFollowing

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R

class organizationFollowingList : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var followingAdapter: organizationFollowingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout correctly
        val view = inflater.inflate(R.layout.fragment_organization_following_list, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.followingList_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        return view
    }
}
