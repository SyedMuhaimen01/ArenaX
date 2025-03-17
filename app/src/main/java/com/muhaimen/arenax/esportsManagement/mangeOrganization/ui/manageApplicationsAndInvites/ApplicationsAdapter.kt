package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageApplicationsAndInvites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.esportsNotificationData

class ApplicationsAdapter(
    private val notificationList: List<esportsNotificationData>, // List of notifications
    private val onDeleteClickListener: (esportsNotificationData) -> Unit // Callback for delete button
) : RecyclerView.Adapter<ApplicationsAdapter.ApplicationsViewHolder>() {

    // ViewHolder class to hold references to the views
    inner class ApplicationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val teamLogo: ImageView = itemView.findViewById(R.id.teamLogo)
        val notificationTextView: TextView = itemView.findViewById(R.id.notificationTextView)
        val deleteNotificationButton: Button = itemView.findViewById(R.id.deleteNotificationButton)
    }

    // Inflate the layout and return the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false) // Replace with your layout file name
        return ApplicationsViewHolder(view)
    }

    // Bind data to the views in the ViewHolder
    override fun onBindViewHolder(holder: ApplicationsViewHolder, position: Int) {
        val notification = notificationList[position]

        // Fetch and load the user's profile picture from Firebase
        notification.userId?.let { fetchUserProfilePicture(it, holder.teamLogo) }

        // Set the notification text
        holder.notificationTextView.text = notification.content

        // Set click listener for the delete button
        holder.deleteNotificationButton.setOnClickListener {
            onDeleteClickListener(notification) // Trigger the callback with the clicked item
        }
    }

    // Return the size of the notification list
    override fun getItemCount(): Int = notificationList.size

    private fun fetchUserProfilePicture(userId: String, imageView: ImageView) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("userData/$userId")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfilePictureUrl = snapshot.child("userProfilePicture").getValue(String::class.java)
                if (!userProfilePictureUrl.isNullOrEmpty()) {
                    // Load the profile picture using Glide
                    Glide.with(imageView.context)
                        .load(userProfilePictureUrl)
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
}