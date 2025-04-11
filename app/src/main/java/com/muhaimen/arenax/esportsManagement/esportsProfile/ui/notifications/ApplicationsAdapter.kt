package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.notifications

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.esportsNotificationData
import com.muhaimen.arenax.esportsManagement.OtherOrganization.OtherOrganization
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.inbox.Threads.organizationChatActivity
import com.muhaimen.arenax.userProfile.otherUserProfile
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONObject

class ApplicationsAdapter(
    private val context: Context,
    private val notificationList: MutableList<esportsNotificationData>, // List of notifications
    private val onDeleteClickListener: (String)-> Unit // Callback for delete button
) : RecyclerView.Adapter<ApplicationsAdapter.ApplicationsViewHolder>() {

    var organizationName: String = ""
    val organizationId: String = ""
    var organizationLocation: String = ""
    var organizationEmail: String = ""
    var organizationPhone: String = ""
    var organizationWebsite: String = ""
    var organizationIndustry: String = ""
    var organizationType: String = ""
    var organizationSize: String = ""
    var organizationTagline: String = ""
    var organizationDescription: String = ""
    var organizationLogo: String = ""
    var followersCount: String = ""
    var followingCount: String = ""

    private val requestQueue = Volley.newRequestQueue(context)

    // ViewHolder class to hold references to the views
    inner class ApplicationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userPicture: ImageView = itemView.findViewById(R.id.userPicture)
        val notificationTextView: TextView = itemView.findViewById(R.id.notificationTextView)
        val deleteNotificationButton: Button = itemView.findViewById(R.id.deleteNotificationButton)

        init {
            // Set click listener for the entire notification item
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val notification = notificationList[position]
                    showOptionsDialog(notification.userId)
                }
            }
        }

        fun bind(notification: esportsNotificationData) {
            notificationTextView.text = notification.content

        }
    }

    // Inflate the layout and return the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.notification_item,
                parent,
                false
            ) // Replace with your layout file name
        return ApplicationsViewHolder(view)
    }

    // Bind data to the views in the ViewHolder
    override fun onBindViewHolder(holder: ApplicationsViewHolder, position: Int) {
        val notification = notificationList[position]

        // Fetch and load the user's profile picture from Firebase
        notification.userId.let { fetchUserProfilePicture(it, holder.userPicture) }

        fetchOrganizationDetails(notification.organizationName)
        // Set the notification text
        holder.notificationTextView.text = notification.content

        // Set click listener for the delete button
        holder.deleteNotificationButton.setOnClickListener {
            onDeleteClickListener(notification.notificationId) // Trigger the callback with the clicked item
        }
    }

    // Return the size of the notification list
    override fun getItemCount(): Int = notificationList.size


    private fun fetchUserProfilePicture(userId: String, imageView: ImageView) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("userData/$userId")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfilePictureUrl =
                    snapshot.child("profilePicture").getValue(String::class.java)
                if (!userProfilePictureUrl.isNullOrEmpty()) {
                    // Load the profile picture using Glide
                    Glide.with(imageView.context)
                        .load(userProfilePictureUrl)
                        .circleCrop()
                        .placeholder(R.drawable.battlegrounds_icon_background) // Placeholder image while loading
                        .error(R.drawable.battlegrounds_icon_background) // Error image if loading fails
                        .into(imageView)
                } else {
                    // If no profile picture exists, use a default image
                    Glide.with(imageView.context)
                        .load(R.drawable.battlegrounds_icon_background)
                        .into(imageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors (e.g., log or show a default image)
                Glide.with(imageView.context)
                    .load(R.drawable.battlegrounds_icon_background)
                    .into(imageView)
            }
        })
    }
    private fun getOrganizationDetails() {
        // Reference to the organizationsData node in Firebase
        val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")

        // Query to find the organization by its name
        val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(organizationName)

        // Execute the query to find the organization
        orgQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate through the results (though there should only be one match)
                    for (orgSnapshot in snapshot.children) {
                        // Retrieve the organization ID
                        val orgId = orgSnapshot.key

                        // Fetch notifications for this organization
                        if (!orgId.isNullOrEmpty()) {
                            fetchOrganizationDataAndStartChat(orgId)

                        } else {
                            println("Organization ID is null or empty")
                        }
                    }
                } else {
                    // Handle the case where no organization is found with the given name
                    println("No organization found with the name: $organizationName")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors when fetching data
                println("Database error: ${error.message}")
            }
        })
    }

    private fun getOrganizationDetails2() {
        // Reference to the organizationsData node in Firebase
        val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")

        // Query to find the organization by its name
        val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(organizationName)

        // Execute the query to find the organization
        orgQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate through the results (though there should only be one match)
                    for (orgSnapshot in snapshot.children) {
                        // Retrieve the organization ID
                        val orgId = orgSnapshot.key

                        // Fetch notifications for this organization
                        if (!orgId.isNullOrEmpty()) {
                            viewOrganizationProfile(organizationName,orgId)

                        } else {
                            println("Organization ID is null or empty")
                        }
                    }
                } else {
                    // Handle the case where no organization is found with the given name
                    println("No organization found with the name: $organizationName")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors when fetching data
                println("Database error: ${error.message}")
            }
        })
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
        val intent = Intent(context, organizationChatActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("fullname", fullname)
            putExtra("gamerTag", gamerTag)
            putExtra("profilePicture", profilePicture)
            putExtra("gamerRank", gamerRank)
            putExtra("dataType",dataType)
        }
        startActivity(context, intent, null)
    }

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
                    organizationName = response.optString("organization_name", "N/A")
                    organizationLocation = response.optString("organization_location", "N/A")
                    organizationEmail = response.optString("organization_email", "N/A")
                    organizationPhone = response.optString("organization_phone", "N/A")
                    organizationWebsite = response.optString("organization_website", "N/A")
                    organizationIndustry = response.optString("organization_industry", "N/A")
                    organizationType = response.optString("organization_type", "N/A")
                    organizationSize = response.optString("organization_size", "N/A")
                    organizationTagline = response.optString("organization_tagline", "N/A")
                    organizationDescription = response.optString("organization_description", "N/A")

                    followersCount = response.optInt("followers", 0).toString()
                    followingCount = response.optInt("following", 0).toString()

                    organizationLogo =
                        response.optString("organization_logo", "").takeIf { it.isNotBlank() }
                            .toString()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            })

        requestQueue.add(jsonObjectRequest)
    }

    fun updateData(notification: List<esportsNotificationData>) {
        notificationList.clear()
        notificationList.addAll(notification)
        notifyDataSetChanged()
    }

    // Show dialog with options: View User Profile and Start Chat
    private fun showOptionsDialog(userId: String) {
        val options = arrayOf("View Organization Profile", "Start Chat")

        val builder =  android.app.AlertDialog. Builder(context, android.R.style.ThemeOverlay_Material_Dark_ActionBar)
        builder.setTitle("Select an Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 ->  getOrganizationDetails2()// Call the function to view user profile
                1 -> getOrganizationDetails() // Call the function to start chat
            }
        }
        val dialog = builder.create()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    // Placeholder function for viewing user profile
    private fun viewOrganizationProfile(organizationName: String, organizationId: String) {
        val intent = Intent(context, OtherOrganization::class.java)
        intent.putExtra("organizationId", organizationId)
        intent.putExtra("organization_name", organizationName)
        context.startActivity(intent)
        // Implement this function to handle viewing the user profile
    }
}