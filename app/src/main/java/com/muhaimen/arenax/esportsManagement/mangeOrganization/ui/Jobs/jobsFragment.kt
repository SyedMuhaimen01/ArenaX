package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

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
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.jobPosting.jobPosting

class jobsFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var postJobButton: FloatingActionButton

    companion object {
        fun newInstance() = jobsFragment()
    }

    private val viewModel: JobsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve organization name from arguments
        val organizationName = arguments?.getString("organization_name") ?: ""

        // Initialize TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = true

        // Pass organizationName to ViewPager Adapter
        val adapter = jobsViewPagerAdapter(requireActivity(), organizationName)
        viewPager.adapter = adapter

        // Attach TabLayout with ViewPager2 using TabLayoutMediator
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Open"
                1 -> "Closed"
                else -> null
            }
        }.attach()

        postJobButton = view.findViewById(R.id.postButton)
        postJobButton.setOnClickListener {
            val intent = Intent(context, jobPosting::class.java).apply {
                putExtra("organization_name", organizationName)
            }
            startActivity(intent)
        }
    }
}
