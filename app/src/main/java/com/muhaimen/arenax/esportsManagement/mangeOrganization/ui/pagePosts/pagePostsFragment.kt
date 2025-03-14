package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.pagePost
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts.uploadPosts.organizationPostActivity

class pagePostsFragment : Fragment() {
    private lateinit var newPostButton: FloatingActionButton
    private lateinit var pagePostsAdapter: pagePostsAdapter
    private lateinit var recyclerView: RecyclerView
    private val postsList = mutableListOf<pagePost>()
    private var organizationName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_page_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve organization name from Bundle (arguments)
        organizationName = arguments?.getString("organization_name")
        Log.d("pagePostsFragment", "Organization name: $organizationName")

        recyclerView = view.findViewById(R.id.pagePostsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        pagePostsAdapter = pagePostsAdapter(recyclerView, postsList)
        recyclerView.adapter = pagePostsAdapter

        newPostButton = view.findViewById(R.id.postButton)
        newPostButton.setOnClickListener {
            val intent = Intent(context, organizationPostActivity::class.java).apply {
                putExtra("organization_name", organizationName)
            }
            startActivity(intent)
        }
    }
}
