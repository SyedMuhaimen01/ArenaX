package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.ClosedJobs
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.OpenJobs

class userJobsViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2 // Two tabs: Open and Closed Jobs

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> OpenUserJobs()
            1 -> ClosedUserJobs()
            else -> OpenJobs()
        }


        return fragment
    }
}