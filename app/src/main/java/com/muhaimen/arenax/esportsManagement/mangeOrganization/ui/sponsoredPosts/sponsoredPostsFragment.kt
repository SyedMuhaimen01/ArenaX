package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.sponsoredPosts

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.muhaimen.arenax.R

class sponsoredPostsFragment : Fragment() {

    companion object {
        fun newInstance() = sponsoredPostsFragment()
    }

    private val viewModel: SponsoredPostsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_sponsored_posts, container, false)
    }
}