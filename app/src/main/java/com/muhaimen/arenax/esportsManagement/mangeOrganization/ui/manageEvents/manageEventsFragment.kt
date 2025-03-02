package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents.schedulingEvent.schedulingEvent

class manageEventsFragment : Fragment() {
    private lateinit var scheduleEventButton:FloatingActionButton
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    companion object {
        fun newInstance() = manageEventsFragment()
    }

    private val viewModel: ManageEventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_manage_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = true
        // Set up ViewPager2 with an adapter (replace with your adapter)
        viewPager.adapter = eventsViewPagerAdapter(requireActivity())
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Upcoming & Ongoing "
                1 -> "closed "
                else -> null

            }
        }.attach()

        scheduleEventButton=view.findViewById(R.id.postButton)
        scheduleEventButton.setOnClickListener {
            val intent= Intent(activity, schedulingEvent::class.java)
            startActivity(intent)
        }
    }
}