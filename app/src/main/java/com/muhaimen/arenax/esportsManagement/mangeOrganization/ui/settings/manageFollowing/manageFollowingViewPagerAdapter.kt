package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageFollowing

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class manageFollowingViewPagerAdapter (fragmentActivity: FragmentActivity, private val bundle: Bundle) : FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> organizationFollowingList()
            1 -> organizationFollowersList()
            else -> organizationFollowingList()
        }

        // Pass the bundle to the fragment
        fragment.arguments = bundle

        return fragment
    }

}