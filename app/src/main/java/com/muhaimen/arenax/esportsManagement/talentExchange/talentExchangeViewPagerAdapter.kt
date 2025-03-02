package com.muhaimen.arenax.esportsManagement.talentExchange

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class talentExchangeViewPagerAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                EmployeesFragment()
            }
            1 -> {
                OrganizationsFragment()
            }
            else -> {
                EmployeesFragment()
            }
        }
    }
}