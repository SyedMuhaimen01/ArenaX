package  com.muhaimen.arenax.explore

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
class ExplorePageViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                explorePosts()
            }
            1 -> {
                exploreAccounts()
            }
            else -> {
                explorePosts()
            }
        }
    }
}