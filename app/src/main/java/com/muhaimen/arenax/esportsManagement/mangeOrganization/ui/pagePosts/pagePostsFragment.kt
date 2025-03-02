package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.dataClasses.pagePost
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts.uploadPosts.organizationPostActivity
import com.muhaimen.arenax.uploadContent.UploadContent.Companion.CAMERA_PERMISSION_REQUEST_CODE

class pagePostsFragment : Fragment() {
    private lateinit var newPostButton: FloatingActionButton
    private lateinit var pagePostsAdapter: pagePostsAdapter
    private lateinit var recyclerView: RecyclerView
    private val postsList = mutableListOf<pagePost>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_page_posts, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.pagePostsRecyclerView)
        recyclerView.layoutManager=LinearLayoutManager(context)

        pagePostsAdapter= pagePostsAdapter(recyclerView,postsList)
        recyclerView.adapter=pagePostsAdapter

        newPostButton=view.findViewById(R.id.postButton)
        newPostButton.setOnClickListener {
            val intent=Intent(context, organizationPostActivity::class.java)
            startActivity(intent)
        }
    }




}