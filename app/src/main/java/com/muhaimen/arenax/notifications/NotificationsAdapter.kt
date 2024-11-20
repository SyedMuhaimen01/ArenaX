package com.muhaimen.arenax.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R

data class NotificationItem(
    val profilePicture: Int,
    val username: String
)

class NotificationsAdapter(
    private val context: Context,
    private val notificationList: List<NotificationItem>,
    private val onAcceptClick: (NotificationItem) -> Unit,
    private val onRejectClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder>() {

    inner class NotificationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        val username: TextView = itemView.findViewById(R.id.username)
        val acceptButton: Button = itemView.findViewById(R.id.cancelButton)
        val rejectButton: Button = itemView.findViewById(R.id.rescheduleButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_synergy_card, parent, false)
        return NotificationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        val notificationItem = notificationList[position]

        holder.profilePicture.setImageResource(notificationItem.profilePicture)
        holder.username.text = notificationItem.username

        holder.acceptButton.setOnClickListener {
            onAcceptClick(notificationItem)
        }

        holder.rejectButton.setOnClickListener {
            onRejectClick(notificationItem)
        }
    }

    override fun getItemCount(): Int = notificationList.size
}
