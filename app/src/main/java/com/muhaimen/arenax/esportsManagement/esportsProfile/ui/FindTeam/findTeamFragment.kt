package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam

import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam.recruitmentAdPosting.recruitmentAdPosting
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.jobsViewPagerAdapter

class findTeamFragment : Fragment() {
    private lateinit var postAdButton: FloatingActionButton
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    companion object {
        fun newInstance() = findTeamFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the correct layout for this fragment
        return inflater.inflate(R.layout.fragment_find_team, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = true

        // Pass organizationName to ViewPager Adapter
        val adapter = userJobsViewPagerAdapter(requireActivity())
        viewPager.adapter = adapter

        // Attach TabLayout with ViewPager2 using TabLayoutMediator
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Active"
                1 -> "History"
                else -> null
            }
        }.attach()

        // Initialize the postAdButton and set its click listener
        postAdButton = view.findViewById(R.id.postButton)
        postAdButton.setOnClickListener {
            val intent = Intent(context, recruitmentAdPosting::class.java)
            startActivity(intent)
        }
    }
}