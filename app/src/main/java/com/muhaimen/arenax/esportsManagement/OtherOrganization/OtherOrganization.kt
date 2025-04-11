package com.muhaimen.arenax.esportsManagement.OtherOrganization

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.ChatActivity
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Event
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.dataClasses.JobWithOrganization
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.dataClasses.pagePost
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.TeamsAdapter
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts.pagePostsAdapter
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@Suppress("NAME_SHADOWING")
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
    private lateinit var messageButton: Button
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var jobsRecyclerView: RecyclerView
    private lateinit var requestQueue: RequestQueue
    private val postsList = mutableListOf<pagePost>()
    private var organizationName: String? = null
    private var organizationId: String? = null
    private lateinit var otherPagePostsAdapter: otherPagePostsAdapter
    private lateinit var otherOrganizationJobsAdapter: otherOrganizationJobsAdapter
    private lateinit var eventsAdapter: otherOrganizationEventsAdapter
    private var eventList: MutableList<Event> = mutableListOf()
    private lateinit var teamsRecyclerView: RecyclerView
    private lateinit var teamsAdapter: otherTeamsAdapter
    private val teamsList = mutableListOf<Team>()
    private lateinit var requestAllianceButton: Button
    private var followersCountTextView: TextView? = null
    private var followingCountTextView: TextView? = null
    private var postCountTextView: TextView? = null
    private var jobWithOrgList: MutableList<JobWithOrganization> = mutableListOf()

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

        messageButton=findViewById(R.id.messageButton)
        messageButton.setOnClickListener {
            Log.d("UserProfile", "Message button clicked")
            fetchOrganizationDataAndStartChat(organizationId!!)
        }

        postsRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        otherPagePostsAdapter = otherPagePostsAdapter(fragmentManager = supportFragmentManager,postsRecyclerView,postsList, organizationName.toString())
        postsRecyclerView.adapter = otherPagePostsAdapter

        eventsRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        eventsAdapter = otherOrganizationEventsAdapter(eventList)
        eventsRecyclerView.adapter = eventsAdapter

        jobsRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        otherOrganizationJobsAdapter = otherOrganizationJobsAdapter(jobWithOrgList)
        jobsRecyclerView.adapter = otherOrganizationJobsAdapter

        teamsRecyclerView = findViewById(R.id.teamsRecyclerView)
        teamsRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        // Pass organizationName to the adapter
        teamsAdapter = otherTeamsAdapter(teamsList, organizationName)
        teamsRecyclerView.adapter = teamsAdapter

        if (!organizationName.isNullOrEmpty()) {
            fetchOrganizationDetails(organizationName!!)
            getOrganizationPostCount(organizationName!!)
            fetchOrganizationPosts()
            fetchUpcomingEvents()
            fetchOrganizationJobs()
            fetchAndSetCounts(organizationId!!)
            // Fetch teams for the given organization
            fetchTeams(organizationName!!)
        }

        postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                otherPagePostsAdapter.handlePlayerVisibility()
            }
        })
        val currentUserId = FirebaseManager.getCurrentUserId()
        requestAllianceButton = findViewById(R.id.requestAllianceButton)
        if (currentUserId  != null && organizationId != null) {
            Log.d("UserProfile", "Current User ID: $currentUserId, Received User ID: $organizationId")
            // Check alliance status on activity load
            lifecycleScope.launch(Dispatchers.Main) {
                val allianceStatus = checkIfAlliance(currentUserId, organizationId!!)
                updateButtonState(requestAllianceButton, allianceStatus)
            }
            requestAllianceButton.setOnClickListener {
                handleButtonClick(requestAllianceButton, currentUserId, organizationId!!)
            }
        }

        organizationEmailTextView.setOnClickListener {
            val emailAddress =
                organizationEmailTextView.text

            // Create an intent to send an email
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))

            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
            }

            organizationPhoneTextView.setOnClickListener {
                val phoneNumber = organizationPhoneTextView.text

                // Create an intent to open the dialer
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }

                // Check if there is an app available to handle the intent
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Log.d("DashboardFragment", "No app available to handle the intent")
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateButtonState(button: Button, allianceStatus: String) {
        when (allianceStatus) {
            "accepted" -> {
                button.setBackgroundColor(resources.getColor(R.color.secondaryColor))
                button.text = "Alliance Established"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = false // Disable button for established alliance
                button.background = resources.getDrawable(R.drawable.searchbar)
            }
            "false" -> {
                button.setBackgroundColor(resources.getColor(R.color.primaryColor))
                button.text = "Request Alliance"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.isEnabled = true // Enable button for new alliance request
                button.background = resources.getDrawable(R.drawable.searchbar)
            }
        }
    }

    // Handles Connection's button click logic
    @SuppressLint("RestrictedApi")
    private fun handleButtonClick(button: Button, currentUserId: String, receiverId: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("userData")
        val organizationsRef = database.getReference("organizationsData")

        when (button.text) {
            "Request Alliance" -> {
                // Update button UI to reflect the alliance status
                button.setBackgroundColor(resources.getColor(R.color.hinttextColor))
                button.text = "Alliance Established"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.background = resources.getDrawable(R.drawable.searchbar)
                button.isEnabled = false

                // Create the alliance object
                val allianceEntry = mapOf(
                    "status" to "accepted",
                    "receiverId" to receiverId
                )

                val followerEntry = mapOf(
                    "status" to "accepted",
                    "followerId" to currentUserId
                )

                // Update current user's following node
                val currentUserFollowingRef = userRef.child(currentUserId).child("synerG").child("following").child(receiverId)
                // Update organization's followers node
                val receiverFollowersRef = organizationsRef.child(receiverId).child("synerG").child("followers").child(currentUserId)

                // Perform both updates simultaneously
                val updates = mapOf(
                    currentUserFollowingRef.path.toString() to allianceEntry,
                    receiverFollowersRef.path.toString() to followerEntry
                )

                database.reference.updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Alliance", "Alliance created with $receiverId and added to both nodes")
                        } else {
                            Log.e("Alliance", "Failed to create alliance: ${task.exception?.message}")
                            // Revert button state in case of failure
                            button.text = "Request Alliance"
                            button.setBackgroundColor(resources.getColor(R.color.primaryColor))
                            button.isEnabled = true
                        }
                    }
            }
            "Alliance Established" -> {
                // Update button UI to reflect the removal of the alliance
                button.setBackgroundColor(resources.getColor(R.color.primaryColor))
                button.text = "Request Alliance"
                button.setTextColor(resources.getColor(R.color.textColor))
                button.background = resources.getDrawable(R.drawable.searchbar)
                button.isEnabled = true

                // Remove entries from both following and followers nodes
                val currentUserFollowingRef = userRef.child(currentUserId).child("synerG").child("following").child(receiverId)
                val receiverFollowersRef = organizationsRef.child(receiverId).child("synerG").child("followers").child(currentUserId)

                // Perform both deletions simultaneously
                val deletions = mapOf(
                    currentUserFollowingRef.path.toString() to null,
                    receiverFollowersRef.path.toString() to null
                )

                database.reference.updateChildren(deletions)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Alliance", "Alliance removed for $receiverId and removed from both nodes")
                        } else {
                            Log.e("Alliance", "Failed to remove alliance: ${task.exception?.message}")
                            // Revert button state in case of failure
                            button.text = "Allied"
                            button.setBackgroundColor(resources.getColor(R.color.hinttextColor))
                            button.isEnabled = false
                        }
                    }
            }
        }
    }

    suspend fun checkIfAlliance(currentUserId: String, organizationId: String): String {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("userData")

        return try {
            val dataSnapshot = userRef.child(currentUserId)
                .child("synerG")
                .child("following")
                .child(organizationId)
                .get()
                .await()

            if (dataSnapshot.exists()) {
                val status = dataSnapshot.child("status").value?.toString() ?: "false"
                status // Return "accepted", "pending", or unexpected values directly
            } else {
                "false" // No alliance exists
            }
        } catch (e: Exception) {
            Log.e("AllianceCheck", "Failed to check alliance: ${e.message}")
            "false"
        }
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

    private fun fetchOrganizationDataAndStartChat(receiverId: String) {
        val database = FirebaseManager.getDatabseInstance()
        val orgRef = database.getReference("organizationsData").child(receiverId)

        orgRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                // Found organization in organizationData
                val profileImageUrl = dataSnapshot.child("organizationLogo").value?.toString().orEmpty()
                val orgName = dataSnapshot.child("organizationName").value?.toString().orEmpty()
                Log.d("organizationName", orgName)

                if (orgName.isNotEmpty()) {
                    startChat(receiverId, orgName, "", profileImageUrl, "00","organization")
                } else {
                    Log.e("Chat", "Organization data is missing fields.")
                }
            } else {
                Log.d("Chat", "Receiver ID not found in userData or organizationData")
            }
        }.addOnFailureListener {
            Log.e("Chat", "Failed to fetch organization data: ${it.message}")
        }
    }

    private fun startChat(
        userId: String,
        fullname: String,
        gamerTag: String,
        profilePicture: String,
        gamerRank: String,
        dataType:String
    ) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("fullname", fullname)
            putExtra("gamerTag", gamerTag)
            putExtra("profilePicture", profilePicture)
            putExtra("gamerRank", gamerRank)
            putExtra("dataType",dataType)
        }
        startActivity(intent)
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


    private fun fetchUpcomingEvents() {
        if (organizationName.isNullOrEmpty()) {
            Toast.makeText(this, "Organization not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "${Constants.SERVER_URL}manageEvents/fetchUpcomingOrganizationEvents"

        val requestQueue = Volley.newRequestQueue(this)

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
                    eventList = parseEventResponse(eventsArray)
                    eventsAdapter.updateData(eventList)
                } else {
                    Toast.makeText(this, "No events found!", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Failed to load events: ${error.message}", Toast.LENGTH_SHORT).show()
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

    private fun fetchTeams(organizationName: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "${Constants.SERVER_URL}manageTeams/teams"

        // Create a JSON object with the organization name to send in the request body
        val requestBody = JSONObject()
        try {
            requestBody.put("organizationName", organizationName)
        } catch (e: JSONException) {
            Log.e("TeamsFragment", "Error creating JSON request body", e)
            Toast.makeText(this, "Failed to create request body", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    val teamsArray = response.optJSONArray("teams") ?: JSONArray()  // Avoid null exception
                    // Clear the list before adding new data
                    teamsList.clear()

                    for (i in 0 until teamsArray.length()) {
                        val teamObject = teamsArray.optJSONObject(i) ?: JSONObject()  // Ensure it's a valid JSON object
                        Log.d("TeamsFragment", "Team object: $teamObject")

                        // Ensure non-null values and extract them properly
                        val teamName = teamObject.optString("team_name", "Unknown Team Name")
                        val gameName = teamObject.optString("game_name", "Unknown Game")
                        val teamDetails = teamObject.optString("team_details", "No Details Available")
                        val teamLocation = teamObject.optString("team_location", "Unknown Location")
                        val teamEmail = teamObject.optString("team_email", "No Email")
                        val teamCaptain = teamObject.optString("team_captain", "No Captain")
                        val teamTagLine = teamObject.optString("team_tagline", "No Tagline")
                        val teamAchievements = teamObject.optString("team_achievements", "No Achievements")
                        val teamLogo = teamObject.optString("team_logo", "")

                        // Ensure teamMembers are handled properly
                        val teamMembers = getTeamMembers(teamObject.optJSONArray("team_members") ?: JSONArray()) // Safely handle missing members

                        // Create the Team object
                        val team = Team(
                            teamName = teamName,
                            gameName = gameName,
                            teamDetails = teamDetails,
                            teamLocation = teamLocation,
                            teamEmail = teamEmail,
                            teamCaptain = teamCaptain,
                            teamTagLine = teamTagLine,
                            teamAchievements = teamAchievements,
                            teamLogo = teamLogo,
                            teamMembers = teamMembers
                        )

                        // Add the team to the list
                        teamsList.add(team)
                        Log.d("TeamsFragment", "Team: ${team.teamName}, ${team.gameName}, ${team.teamLogo}")
                    }

                    // Notify the adapter of changes
                    teamsAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    Log.e("TeamsFragment", "Error parsing teams response", e)
                }
            },
            { error ->
                Log.e("TeamsFragment", "Error fetching teams: $error")
            }
        )

        // Add the request to the request queue
        queue.add(jsonObjectRequest)
    }

    private fun getTeamMembers(teamMembersArray: JSONArray): List<String> {
        val membersList = mutableListOf<String>()
        for (i in 0 until teamMembersArray.length()) {
            membersList.add(teamMembersArray.optString(i, ""))
        }
        return membersList
    }

    private fun getOrganizationPostCount(organizationName: String) {
        val url = "${Constants.SERVER_URL}organizationPosts/postCount"

        val jsonBody = JSONObject().apply {
            put("organization_name", organizationName)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            { response ->
                val postCount = response.optInt("postCount", 0)
                postCountTextView?.text = postCount.toString()
            },
            { error ->
                Toast.makeText(this, "Failed to fetch post count: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(request)
    }

    private fun fetchAndSetCounts(orgId: String) {
        val followersRef = FirebaseDatabase.getInstance().getReference("organizationsData/$orgId/synerG/followers")
        val followingRef = FirebaseDatabase.getInstance().getReference("organizationsData/$orgId/synerG/following")


        // Fetch and count followers with status "accepted"
        followersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val acceptedFollowersCount = snapshot.children.count {
                    it.child("status").value?.toString() == "accepted"
                }
                followersCountTextView?.text = acceptedFollowersCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Error fetching followers: ${error.message}")
            }
        })

        // Fetch and count following with status "accepted"
        followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val acceptedFollowingCount = snapshot.children.count {
                    it.child("status").value?.toString() == "accepted"
                }
                followingCountTextView?.text   = acceptedFollowingCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Error fetching following: ${error.message}")
            }
        })
    }

    private fun fetchOrganizationJobs() {
        val queue = Volley.newRequestQueue(this)
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
        otherOrganizationJobsAdapter.updateData(jobWithOrgList)

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
            Toast.makeText(this, "Error parsing job data", Toast.LENGTH_SHORT).show()
        }

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
        followersCountTextView = findViewById(R.id.followersCountTextView)
        followingCountTextView = findViewById(R.id.followingCountTextView)
        postCountTextView = findViewById(R.id.postsCountTextView)

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