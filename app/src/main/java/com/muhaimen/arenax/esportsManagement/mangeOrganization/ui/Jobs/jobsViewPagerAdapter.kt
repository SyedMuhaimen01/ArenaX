package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class jobsViewPagerAdapter(fragmentActivity: FragmentActivity, private val organizationName: String) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2 // Two tabs: Open and Closed Jobs

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> OpenJobs()
            1 -> ClosedJobs()
            else -> OpenJobs()
        }

        // Pass organization_name to the fragment
        fragment.arguments = Bundle().apply {
            putString("organization_name", organizationName)
        }

        return fragment
    }
}
