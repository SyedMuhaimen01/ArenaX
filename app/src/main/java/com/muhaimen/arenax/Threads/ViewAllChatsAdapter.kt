package com.muhaimen.arenax.Threads

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem
import com.muhaimen.arenax.utils.FirebaseManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewAllChatsAdapter(
    private var chatList: List<ChatItem>
) : RecyclerView.Adapter<ViewAllChatsAdapter.ChatViewHolder>() {

    // ViewHolder class to hold and bind the views for each item
    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profilePicture)
        val usernameTextView: TextView = view.findViewById(R.id.fullname)
        val timeTextView: TextView = view.findViewById(R.id.time)
        val newMsgIndicatorTextView: TextView = view.findViewById(R.id.newMsgIndicator)

        init {
            itemView.setOnClickListener {
                // Get the current user data based on the position
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chatItem = chatList[position]
                    // Determine the other user's ID
                    val receiverId = if (chatItem.senderId == FirebaseManager.getCurrentUserId()) {
                        chatItem.receiverId
                    } else {
                        chatItem.senderId
                    }
                    // Fetch selected user data and start ChatActivity
                    fetchUserDataAndStartChat(receiverId)
                }
            }
        }

        private fun fetchUserDataAndStartChat(receiverId: String) {
            val database = FirebaseManager.getDatabseInstance()
            val userRef = database.getReference("userData").child(receiverId)

            userRef.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    // Fetch only the necessary user data
                    val profileImageUrl = dataSnapshot.child("profilePicture").value?.toString() ?: ""
                    val fullname = dataSnapshot.child("fullname").value?.toString() ?: "Unknown User"
                    val gamerTag = dataSnapshot.child("gamerTag").value?.toString() ?: "Unknown GamerTag"

                    // Create intent and pass user data
                    val intent = Intent(itemView.context, ChatActivity::class.java).apply {
                        putExtra("userId", receiverId)
                        putExtra("fullname", fullname)
                        putExtra("gamerTag", gamerTag)
                        putExtra("profilePicture", profileImageUrl)
                        putExtra("gamerRank", "00") // You can modify this based on your logic
                    }
                    itemView.context.startActivity(intent)
                }
            }.addOnFailureListener {
                // Handle failure to retrieve data
                val intent = Intent(itemView.context, ChatActivity::class.java).apply {
                    putExtra("userId", receiverId)
                    putExtra("fullname", "Unknown User")
                    putExtra("gamerTag", "Unknown GamerTag")
                    putExtra("profilePicture", "null") // or a placeholder
                    putExtra("gamerRank", "00") // You can modify this based on your logic
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_all_chats_card, parent, false)
        return ChatViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = chatList[position]

        // Determine the other user's ID
        val otherUserId = if (chatItem.senderId == FirebaseManager.getCurrentUserId()) {
            chatItem.receiverId
        } else {
            chatItem.senderId
        }

        // Fetch the other user's profile picture from Firebase using their user ID
        val database = FirebaseManager.getDatabseInstance()
        val userRef = database.getReference("userData").child(otherUserId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val profileImageUrl = dataSnapshot.child("profilePicture").value?.toString() ?: ""
                holder.usernameTextView.text = dataSnapshot.child("fullname").value?.toString() ?: "Unknown User"

                // Load profile image using Glide
                Glide.with(holder.profileImage.context)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.game_icon_foreground)
                    .error(R.drawable.game_icon_foreground)
                    .circleCrop()
                    .into(holder.profileImage)
            } else {
                holder.usernameTextView.text = "Unknown User"
                holder.profileImage.setImageResource(R.drawable.game_icon_foreground)
            }
        }.addOnFailureListener {
            holder.usernameTextView.text = "Unknown User"
            holder.profileImage.setImageResource(R.drawable.game_icon_foreground)
        }

        // Set the latest message time
        val formattedDate = convertTimestampToDateWithAmPm(chatItem.time)
        holder.timeTextView.text = formattedDate

        // Conditionally show or hide the new message indicator
        holder.newMsgIndicatorTextView.visibility = if (chatItem.time > chatItem.lastReadTime) {
            holder.newMsgIndicatorTextView.text = "new message" // Consider localization
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun getItemCount(): Int = chatList.size

    // Method to update the chat list
    @SuppressLint("NotifyDataSetChanged")
    fun updateChatList(newChatList: List<ChatItem>) {
        chatList = newChatList
        notifyDataSetChanged()
    }

    fun convertTimestampToDateWithAmPm(timestamp: Long): String {
        // Create a Date object from the timestamp in milliseconds
        val date = Date(timestamp)

        // Define a custom date format: "HH:mm a" (24-hour format with AM/PM)
        val dateFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())

        // Format the date to a readable string
        return dateFormat.format(date)
    }
}
