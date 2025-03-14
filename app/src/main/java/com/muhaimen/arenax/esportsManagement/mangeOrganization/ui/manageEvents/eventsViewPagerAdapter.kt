package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class eventsViewPagerAdapter(fragmentActivity: FragmentActivity, private val organizationName: String?) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> upcommingEvents()
            1 -> closedEvents()
            else -> upcommingEvents()
        }

        // Pass organizationName as an argument to the fragment
        fragment.arguments = Bundle().apply {
            putString("organization_name", organizationName)
        }

        return fragment
    }
}
