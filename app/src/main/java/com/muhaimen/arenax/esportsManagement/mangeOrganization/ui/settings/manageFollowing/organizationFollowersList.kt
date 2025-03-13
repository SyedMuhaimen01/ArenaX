package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageFollowing

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R


class organizationFollowersList : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var followersAdapter: organizationFollowersAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_organization_followers_list, container, false)
        recyclerView = binding.findViewById(R.id.followersList_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding
    }

}