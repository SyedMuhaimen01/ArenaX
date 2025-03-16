package com.muhaimen.arenax.esportsManagement.OtherOrganization

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.pagePost
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts.pagePostsAdapter
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class OtherOrganization : AppCompatActivity() {
    private lateinit var organizationNameTextView: TextView
    private lateinit var organizationLocationTextView: TextView
    private lateinit var organizationEmailTextView: TextView
    private lateinit var organizationPhoneTextView: TextView
    private lateinit var organizationWebsiteTextView: TextView
    private lateinit var organizationIndustryTextView: TextView
    private lateinit var organizationTypeTextView: TextView
    private lateinit var organizationSizeTextView: TextView
    private lateinit var organizationTaglineTextView: TextView
    private lateinit var organizationDescriptionTextView: TextView
    private lateinit var organizationLogoImageView: ImageView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var jobsRecyclerView: RecyclerView
    private lateinit var requestQueue: RequestQueue
    private val postsList = mutableListOf<pagePost>()
    private var organizationName: String? = null
    private var organizationId: String? = null
    private lateinit var otherPagePostsAdapter: otherPagePostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_other_organization)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        window.statusBarColor = resources.getColor(R.color.primaryColor)

        requestQueue = Volley.newRequestQueue(this)
        initializeViews()
        organizationName = intent.getStringExtra("organization_name")
        organizationId = intent.getStringExtra("organizationId")
        Log.d("DashboardFragment", "Organization name: $organizationName")

        postsRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        otherPagePostsAdapter = otherPagePostsAdapter(postsRecyclerView,postsList, organizationName.toString())
        postsRecyclerView.adapter = otherPagePostsAdapter

        if (!organizationName.isNullOrEmpty()) {
            fetchOrganizationDetails(organizationName!!)
            fetchOrganizationPosts()
        }

        postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                otherPagePostsAdapter.handlePlayerVisibility()
            }
        })

    }

    @SuppressLint("SetTextI18n")
    private fun fetchOrganizationDetails(orgName: String) {
        Log.d("DashboardFragment", "Fetching organization details for: $orgName")
        val url = "${Constants.SERVER_URL}registerOrganization/organizationDetails"

        val requestBody = JSONObject().apply {
            put("organizationName", orgName)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    organizationNameTextView?.text = response.optString("organization_name", "N/A")
                    organizationLocationTextView?.text = response.optString("organization_location", "N/A")
                    organizationEmailTextView?.text = response.optString("organization_email", "N/A")
                    organizationPhoneTextView?.text = response.optString("organization_phone", "N/A")
                    organizationWebsiteTextView?.text = response.optString("organization_website", "N/A")
                    organizationIndustryTextView?.text = response.optString("organization_industry", "N/A")
                    organizationTypeTextView?.text = response.optString("organization_type", "N/A")
                    organizationSizeTextView?.text = response.optString("organization_size", "N/A")
                    organizationTaglineTextView?.text = response.optString("organization_tagline", "N/A")
                    organizationDescriptionTextView?.text = response.optString("organization_description", "N/A")


                    val logoUrl = response.optString("organization_logo", "").takeIf { it.isNotBlank() }

                    organizationLogoImageView?.let {
                        Glide.with(this)
                            .load(logoUrl ?: R.drawable.add_icon_foreground)
                            .placeholder(R.drawable.add_icon_foreground)
                            .into(it)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Failed to fetch organization details", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(jsonObjectRequest)
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
                    otherPagePostsAdapter.notifyDataSetChanged()

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


    private fun initializeViews() {
        organizationNameTextView = findViewById(R.id.organizationNameTextView)
        organizationLocationTextView = findViewById(R.id.organizationLocationTextView)
        organizationEmailTextView = findViewById(R.id.organizationEmailTextView)
        organizationIndustryTextView = findViewById(R.id.organizationIndustryTextView)
        organizationWebsiteTextView = findViewById(R.id.organizationWebsiteTextView)
        organizationPhoneTextView = findViewById(R.id.organizationPhoneTextView)
        organizationTypeTextView = findViewById(R.id.organizationTypeTextView)
        organizationSizeTextView = findViewById(R.id.organizationSizeTextView)
        organizationTaglineTextView = findViewById(R.id.organizationTaglineTextView)
        organizationDescriptionTextView = findViewById(R.id.organizationDescriptionTextView)
        organizationLogoImageView = findViewById(R.id.profilePicture)
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView)
        jobsRecyclerView = findViewById(R.id.jobsRecyclerView)

    }

    override fun onDestroy() {
        super.onDestroy()
        otherPagePostsAdapter.releaseAllPlayers()
    }

    override fun onPause() {
        super.onPause()
        otherPagePostsAdapter.releaseAllPlayers()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        otherPagePostsAdapter.releaseAllPlayers()
    }
}