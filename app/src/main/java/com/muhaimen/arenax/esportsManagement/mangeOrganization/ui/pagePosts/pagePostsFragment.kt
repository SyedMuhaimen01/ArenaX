package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.muhaimen.arenax.R

class pagePostsFragment : Fragment() {

    companion object {
        fun newInstance() = pagePostsFragment()
    }

    private val viewModel: PagePostsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_page_posts, container, false)
    }
}