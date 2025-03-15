package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.pagePost
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts.uploadPosts.organizationPostActivity
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class pagePostsFragment : Fragment() {
    private lateinit var newPostButton: FloatingActionButton
    private lateinit var pagePostsAdapter: pagePostsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var requestQueue: RequestQueue
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

        // Initialize Volley Request Queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // Retrieve organization name from arguments
        organizationName = arguments?.getString("organization_name")
        Log.d("pagePostsFragment", "Organization name: $organizationName")

        recyclerView = view.findViewById(R.id.pagePostsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)  // Set layout manager here

        // Pass organization name to adapter along with the posts list
        pagePostsAdapter = pagePostsAdapter(recyclerView,postsList, organizationName.toString())
        recyclerView.adapter = pagePostsAdapter

        newPostButton = view.findViewById(R.id.postButton)
        newPostButton.setOnClickListener {
            val intent = Intent(context, organizationPostActivity::class.java).apply {
                putExtra("organization_name", organizationName)
            }
            startActivity(intent)
        }

        // Fetch organization posts
        fetchOrganizationPosts()
    }

    private fun fetchOrganizationPosts() {
        if (organizationName.isNullOrEmpty()) {
            Log.e("pagePostsFragment", "Organization name is null or empty.")
            return
        }

        val url = "${Constants.SERVER_URL}organizationPosts/fetchOrganizationPosts"

        val requestBody = JSONObject()
        requestBody.put("organizationName", organizationName)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    val postsArray: JSONArray = response.getJSONArray("posts")
                    val fetchedPosts = mutableListOf<pagePost>()

                    for (i in 0 until postsArray.length()) {
                        val postObj = postsArray.getJSONObject(i)
                        val commentsArray = postObj.getJSONArray("comments")

                        // Parse comments
                        val commentsList = mutableListOf<Comment>()
                        for (j in 0 until commentsArray.length()) {
                            val commentObj = commentsArray.getJSONObject(j)
                            val comment = Comment(
                                commentId = commentObj.getInt("comment_id"),
                                commentText = commentObj.getString("comment"),
                                createdAt = commentObj.getString("created_at"),
                                commenterName = commentObj.getString("commenter_name"),
                                commenterProfilePictureUrl = commentObj.optString("commenter_profile_pic", null)
                            )
                            commentsList.add(comment)
                        }

                        // Parse post
                        val post = pagePost(
                            postId = postObj.getInt("post_id"),
                            postContent = postObj.optString("post_content", null),
                            caption = postObj.optString("caption", null),
                            sponsored = postObj.getBoolean("sponsored"),
                            likes = postObj.getInt("likes"),
                            comments = postObj.getInt("post_comments"),
                            shares = postObj.getInt("shares"),
                            clicks = postObj.getInt("clicks"),
                            city = postObj.optString("city", null),
                            country = postObj.optString("country", null),
                            createdAt = postObj.getString("created_at"),
                            organizationName = postObj.getString("organization_name"),
                            organizationLogo = postObj.optString("organization_logo", null),
                            commentsData = commentsList
                        )

                        fetchedPosts.add(post)
                    }

                    // Update UI
                    postsList.clear()
                    postsList.addAll(fetchedPosts)
                    pagePostsAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    Log.e("pagePostsFragment", "Error parsing response: ${e.message}")
                }
            },
            { error ->
                Log.e("pagePostsFragment", "Volley error: ${error.message}")
            }
        )

        // Add request to the queue
        requestQueue.add(jsonObjectRequest)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requestQueue.cancelAll("fetchOrganizationPosts")
    }
}

