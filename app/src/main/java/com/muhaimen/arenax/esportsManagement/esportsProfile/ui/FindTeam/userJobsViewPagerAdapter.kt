package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class userJobsViewPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OpenUserJobs()
            1 -> ClosedUserJobs()
            else -> OpenUserJobs()
        }
    }
}
