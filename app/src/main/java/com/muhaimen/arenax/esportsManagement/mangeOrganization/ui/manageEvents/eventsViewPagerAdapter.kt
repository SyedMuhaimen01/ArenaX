package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class eventsViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity)  {
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                upcommingEvents()
            }
            1 -> {
                closedEvents()
            }
            else -> {
                upcommingEvents()
            }
        }
    }
}