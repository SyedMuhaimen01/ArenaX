package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.sponsoredPosts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.pagePost
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class SponsoredPostsFragment : Fragment() {

    private lateinit var sponsoredPostsAdapter: SponsoredPostsAdapter
    private lateinit var mediaTypeDropdownSpinner:Spinner
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var jobsRecyclerView: RecyclerView
    private lateinit var requestQueue: RequestQueue
    private val postsList = mutableListOf<pagePost>()
    private var organizationName: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_sponsored_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        // Initialize dropdown menu
        setupDropdownMenu()


        // Initialize Volley Request Queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // Retrieve organization name from arguments
        organizationName = arguments?.getString("organization_name")
        Log.d("pagePostsFragment", "Organization name: $organizationName")


        postsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        sponsoredPostsAdapter= SponsoredPostsAdapter(postsRecyclerView,postsList, organizationName.toString())
        postsRecyclerView.adapter = sponsoredPostsAdapter


        if (!organizationName.isNullOrEmpty()) {
            fetchOrganizationPosts()
        }
        postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                sponsoredPostsAdapter.handlePlayerVisibility()
            }
        })

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
                    sponsoredPostsAdapter.notifyDataSetChanged()

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

    override fun onDestroy() {
        super.onDestroy()
        sponsoredPostsAdapter.releaseAllPlayers()
    }

    override fun onPause() {
        super.onPause()
        sponsoredPostsAdapter.releaseAllPlayers()
    }

    fun onBackPressed() {
        sponsoredPostsAdapter.releaseAllPlayers()
    }
    private fun setupDropdownMenu() {
        // Define dropdown options
        val options = listOf("Posts", "Events", "Jobs")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mediaTypeDropdownSpinner.adapter = adapter

        // Handle item selection
        mediaTypeDropdownSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { // Posts selected
                        postsRecyclerView.visibility = View.VISIBLE
                        eventsRecyclerView.visibility = View.GONE
                        jobsRecyclerView.visibility = View.GONE
                    }
                    1 -> { // Events selected
                        postsRecyclerView.visibility = View.GONE
                        eventsRecyclerView.visibility = View.VISIBLE
                        jobsRecyclerView.visibility = View.GONE
                    }
                    2 -> { // Jobs selected
                        postsRecyclerView.visibility = View.GONE
                        eventsRecyclerView.visibility = View.GONE
                        jobsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun initializeViews(view: View) {
        mediaTypeDropdownSpinner = view.findViewById(R.id.mediaTypeDropdownSpinner)
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView)
        jobsRecyclerView = view.findViewById(R.id.jobsRecyclerView)
    }


}