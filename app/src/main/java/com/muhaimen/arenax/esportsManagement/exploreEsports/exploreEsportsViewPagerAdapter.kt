package com.muhaimen.arenax.esportsManagement.exploreEsports

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class exploreEsportsViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                exploreEmployees()
            }
            1 -> {
                exploreOrganizations()
            }
            else -> {
                exploreEmployees()
            }
        }
    }
}