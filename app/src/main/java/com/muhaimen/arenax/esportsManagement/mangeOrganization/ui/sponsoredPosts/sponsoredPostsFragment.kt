package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.sponsoredPosts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Event
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.dataClasses.JobWithOrganization
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.dataClasses.pagePost
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class SponsoredPostsFragment : Fragment() {

    private lateinit var sponsoredPostsAdapter: SponsoredPostsAdapter
    private lateinit var SponsoredEventsAdapter: SponsoredEventsAdapter
    private lateinit var SponsoredJobsAdapter: SponsoredJobsAdapter
    private lateinit var mediaTypeDropdownSpinner: Spinner
    private lateinit var audienceDropdownSpinner: Spinner
    private lateinit var reachDropdownSpinner: Spinner
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var jobsRecyclerView: RecyclerView
    private lateinit var sponsorButton: Button
    private lateinit var requestQueue: RequestQueue
    private var postsList = mutableListOf<pagePost>()
    private var eventsList = mutableListOf<Event>()
    private var organizationName: String? = null
    private var jobWithOrgList: MutableList<JobWithOrganization> = mutableListOf()
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

        // Setup RecyclerViews
        postsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        sponsoredPostsAdapter = SponsoredPostsAdapter(postsRecyclerView, postsList, organizationName.toString())
        postsRecyclerView.adapter = sponsoredPostsAdapter

        jobsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        SponsoredJobsAdapter = SponsoredJobsAdapter(jobWithOrgList)
        jobsRecyclerView.adapter = SponsoredJobsAdapter

        eventsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        SponsoredEventsAdapter = SponsoredEventsAdapter(eventsList)
        eventsRecyclerView.adapter = SponsoredEventsAdapter

        if (!organizationName.isNullOrEmpty()) {
            fetchOrganizationPosts()
            fetchUpcomingEvents()
            fetchOrganizationJobs()
        }

        postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                sponsoredPostsAdapter.handlePlayerVisibility()
            }
        })

        // Set up Sponsor button click listener
        sponsorButton.setOnClickListener {
            initiateJazzCashPayment()
        }
    }

    private fun initiateJazzCashPayment() {
        val merchantId = "MC150130"
        val password = "YOUR_PASSWORD"
        val integritySalt = "YOUR_INTEGRITY_SALT"

        val orderId = "ORDER${System.currentTimeMillis()}" // Unique order ID
        val amount = "5000" // Amount in PKR (e.g., 5000 = 50 PKR)
        val currency = "PKR"
        val dateTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val returnURL = "myapp://payment-return" // Deep link to your app
        val expiryDateTime = "20231010120000" // Expiry time for the transaction

        // Generate secure hash
        val hashString = "$integritySalt$merchantId$orderId$amount$currency$dateTime$password"
        val hash = MessageDigest.getInstance("SHA-256").digest(hashString.toByteArray()).toHexString()

        // Create payment request payload
        val paymentRequest = JSONObject().apply {
            put("pp_Amount", amount)
            put("pp_BankID", "TBANK")
            put("pp_Currency", currency)
            put("pp_Language", "EN")
            put("pp_MerchantID", merchantId)
            put("pp_OrderID", orderId)
            put("pp_ReturnURL", returnURL)
            put("pp_TxnDateTime", dateTime)
            put("pp_TxnExpiryDateTime", expiryDateTime)
            put("pp_TxnRefNo", "REF${System.currentTimeMillis()}")
            put("pp_Version", "1.1")
            put("pp_SecureHash", hash)
        }

        // Redirect to JazzCash payment gateway
        val jazzCashUrl = "https://sandbox.jazzcash.com.pk/Application/Pay" // Use production URL in live environment
        openJazzCashPaymentPage(jazzCashUrl, paymentRequest)
    }

    private fun openJazzCashPaymentPage(url: String, params: JSONObject) {
        val formBody = StringBuilder()
        params.keys().forEach { key ->
            formBody.append("$key=${params.getString(key)}&")
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun fetchOrganizationJobs() {
        val queue = Volley.newRequestQueue(requireContext())
        val url = "${Constants.SERVER_URL}manageJobs/getOpenJobs"

        val requestBody = JSONObject().apply {
            put("organization_name", organizationName)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    Log.d("Volley", "Response: $response")
                    val jobsArray = response.getJSONArray("jobs")
                    clearAndPopulateAdapter(jobsArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                    //    Toast.makeText(context, "Error parsing job data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("Volley", "Error fetching open jobs: ${error.message}")
                // Toast.makeText(context, "Error fetching open jobs", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(request)
    }
    private fun clearAndPopulateAdapter(response: JSONArray) {

        jobWithOrgList.clear()

        // Parse the response and populate the list
        parseAndPopulateJobs(response)

        // Notify the appropriate adapter of the data change
        SponsoredJobsAdapter.updateData(jobWithOrgList)

    }

    private fun parseAndPopulateJobs(response: JSONArray) {
        try {
            for (i in 0 until response.length()) {
                val jobObject = response.getJSONObject(i)

                // Parse Job data
                val jobId = jobObject.optString("job_id", "")
                val organizationId = jobObject.optString("organization_id", "")
                val jobTitle = jobObject.optString("job_title", "")
                val jobType = jobObject.optString("job_type", "")
                val jobLocation = jobObject.optString("job_location", "")
                val jobDescription = jobObject.optString("job_description", "")
                val workplaceType = jobObject.optString("workplace_type", "")
                val tags = jobObject.getJSONArray("tags").let { tagArray ->
                    List(tagArray.length()) { index -> tagArray.optString(index, "") }
                }

                val job = Job(
                    jobId = jobId,
                    organizationId = organizationId,
                    jobTitle = jobTitle,
                    jobType = jobType,
                    jobLocation = jobLocation,
                    jobDescription = jobDescription,
                    workplaceType = workplaceType,
                    tags = tags
                )

                // Parse Organization data (if available)
                val organizationObject = jobObject.optJSONObject("organization")
                val organization = if (organizationObject != null) {
                    OrganizationData(
                        organizationId = organizationObject.optString("organization_id", ""),
                        organizationName = organizationObject.optString("organization_name", "Unknown Organization"),
                        organizationLogo = organizationObject.optString("organization_logo", null),
                        organizationLocation = organizationObject.optString("organization_location", null)
                    )
                } else {
                    OrganizationData(
                        organizationId = organizationId,
                        organizationName = "Unknown Organization",
                        organizationLogo = null,
                        organizationLocation = null
                    )
                }

                // Combine Job and Organization into a wrapper object
                val jobWithOrg = JobWithOrganization(job, organization)
                jobWithOrgList.add(jobWithOrg)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

    private fun setupDropdownMenu() {
        // Define dropdown options
        var options = listOf("Location-Based", "Interest-Based", "Both")
        var adapter = ArrayAdapter(requireContext(), R.layout.dropdown_spinner_item, options)
        adapter.setDropDownViewResource(R.layout.dropdown_spinner_item)
        audienceDropdownSpinner.adapter = adapter

        options = listOf("25%", "50%", "75%","100%")
        adapter = ArrayAdapter(requireContext(), R.layout.dropdown_spinner_item, options)
        adapter.setDropDownViewResource(R.layout.dropdown_spinner_item)
        reachDropdownSpinner.adapter = adapter
        // Handle item selection
        options = listOf("Posts", "Events", "Jobs")
        adapter = ArrayAdapter(requireContext(), R.layout.dropdown_spinner_item, options)
        adapter.setDropDownViewResource(R.layout.dropdown_spinner_item)
        mediaTypeDropdownSpinner.adapter = adapter
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

    private fun fetchUpcomingEvents() {
        if (organizationName.isNullOrEmpty()) {
            Toast.makeText(context, "Organization not found!", Toast.LENGTH_SHORT).show()
            return
        }
        val url = "${Constants.SERVER_URL}manageEvents/fetchUpcomingOrganizationEvents"
        val requestQueue = Volley.newRequestQueue(requireContext())
        val requestBody = JSONObject().apply {
            put("organization_name", organizationName)
        }
        val jsonRequest = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            Response.Listener { response ->
                val eventsArray = response.optJSONArray("events")
                Log.d("Events111", "Raw JSON Response: $response")
                if (eventsArray != null) {
                    eventsList = parseEventResponse(eventsArray)
                    SponsoredEventsAdapter.updateData(eventsList)
                } else {
                    Toast.makeText(context, "No events found!", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(context, "Failed to load events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Content-Type" to "application/json")
            }
        }
        requestQueue.add(jsonRequest)
    }

    private fun parseEventResponse(response: JSONArray): MutableList<Event> {
        val events = mutableListOf<Event>()
        for (i in 0 until response.length()) {
            val obj = response.getJSONObject(i)
            val event = Event(
                eventId = obj.getString("event_id"),
                organizationId = obj.getString("organization_id"),
                eventName = obj.getString("event_name"),
                gameName = obj.getString("game_name"),
                eventMode = obj.getString("event_mode"),
                platform = obj.getString("platform"),
                location = obj.optString("location", ""),
                eventDescription = obj.optString("event_description", ""),
                startDate = obj.optString("start_date", ""),
                endDate = obj.optString("end_date", ""),
                startTime = obj.optString("start_time", ""),
                endTime = obj.optString("end_time", ""),
                eventLink = obj.optString("event_link", ""),
                eventBanner = obj.optString("event_banner", "")
            )
            events.add(event)
        }
        return events
    }

    private fun initializeViews(view: View) {
        mediaTypeDropdownSpinner = view.findViewById(R.id.mediaTypeDropdownSpinner)
        audienceDropdownSpinner= view.findViewById(R.id.audienceTargetingSpinner)
        reachDropdownSpinner = view.findViewById(R.id.reachPercentageSpinner)
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView)
        jobsRecyclerView = view.findViewById(R.id.jobsRecyclerView)
        sponsorButton = view.findViewById(R.id.sponsorButton)
    }

    fun onBackPressed() {
        sponsoredPostsAdapter.releaseAllPlayers()
    }

    override fun onResume() {
        super.onResume()
        sponsoredPostsAdapter.handlePlayerVisibility()
    }

    override fun onStart() {
        super.onStart()
        sponsoredPostsAdapter.handlePlayerVisibility()
    }

    override fun onStop() {
        super.onStop()
        sponsoredPostsAdapter.releaseAllPlayers()
    }

    override fun onDestroy() {
        super.onDestroy()
        sponsoredPostsAdapter.releaseAllPlayers()
    }

    override fun onPause() {
        super.onPause()
        sponsoredPostsAdapter.releaseAllPlayers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requestQueue.cancelAll("fetchOrganizationPosts")
    }
}

// Helper function to convert ByteArray to Hex String
fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}