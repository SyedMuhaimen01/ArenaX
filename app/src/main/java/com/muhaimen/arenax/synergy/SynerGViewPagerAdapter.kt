package  com.muhaimen.arenax.synergy

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
class SynerGViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                manageFollowingList()
            }
            1 -> {
                manageFollowersList()
            }
            else -> {
                manageFollowingList()
            }
        }
    }
}