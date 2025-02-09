package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts.uploadPosts.organizationPostActivity

class pagePostsFragment : Fragment() {
    private lateinit var newPostButton: FloatingActionButton
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newPostButton = view.findViewById(R.id.postButton)
        newPostButton.setOnClickListener {
            val intent=Intent(context, organizationPostActivity::class.java)
            startActivity(intent)
        }
    }
}