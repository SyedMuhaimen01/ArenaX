package com.muhaimen.arenax.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.NotificationsItem

class NotificationsAdapter(
    private val context: Context,
    private val notificationList: MutableList<NotificationsItem>,
    private val onAcceptClick: (NotificationsItem) -> Unit,
    private val onRejectClick: (NotificationsItem) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder>() {

    inner class NotificationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        val username: TextView = itemView.findViewById(R.id.username)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val rejectButton: Button = itemView.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_synergy_card, parent, false)
        return NotificationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        val notificationItem = notificationList[position]

        // Load profile picture using Glide
        Glide.with(context)
            .load(notificationItem.profilePicture)
            .placeholder(R.drawable.add_icon_foreground) // Placeholder image
            .into(holder.profilePicture)

        // Set username
        holder.username.text = notificationItem.username

        // Handle accept button click
        holder.acceptButton.setOnClickListener {
            onAcceptClick(notificationItem)
        }

        // Handle reject button click
        holder.rejectButton.setOnClickListener {
            onRejectClick(notificationItem)
        }
    }

    override fun getItemCount(): Int = notificationList.size
}
