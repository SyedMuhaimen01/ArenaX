package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class jobsViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity)  {
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                OpenJobs()
            }
            1 -> {
                ClosedJobs()
            }
            else -> {
                OpenJobs()
            }
        }
    }
}