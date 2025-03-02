package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageFollowing

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class manageFollowingViewPagerAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                organizationFollowingList()
            }
            1 -> {
                organizationFollowersList()
            }
            else -> {
                organizationFollowingList()
            }
        }
    }
}