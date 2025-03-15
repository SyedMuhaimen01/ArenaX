package com.muhaimen.arenax.Threads

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem
import com.muhaimen.arenax.utils.FirebaseManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewAllChatsAdapter(
    private var chatList: List<ChatItem>
) : RecyclerView.Adapter<ViewAllChatsAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profilePicture)
        val usernameTextView: TextView = view.findViewById(R.id.fullname)
        val timeTextView: TextView = view.findViewById(R.id.time)
        val newMsgIndicatorTextView: TextView = view.findViewById(R.id.newMsgIndicator)

        init {
            // Handle normal click (to open the chat)
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chatItem = chatList[position]
                    val receiverId = if (chatItem.senderId == FirebaseManager.getCurrentUserId()) {
                        chatItem.receiverId
                    } else {
                        chatItem.senderId
                    }
                    fetchUserDataAndStartChat(receiverId)
                }
            }
            itemView.isLongClickable = true
            // Handle long press to show delete dialog
            itemView.setOnLongClickListener {
                showDeleteChatDialog(itemView.context, chatList[adapterPosition])
                true
            }
        }

        private fun fetchUserDataAndStartChat(receiverId: String) {
            val database = FirebaseManager.getDatabseInstance()
            val userRef = database.getReference("userData").child(receiverId)

            userRef.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    // Found user in userData
                    val profileImageUrl = dataSnapshot.child("profilePicture").value?.toString().orEmpty()
                    val fullname = dataSnapshot.child("fullname").value?.toString().orEmpty()
                    val gamerTag = dataSnapshot.child("gamerTag").value?.toString().orEmpty()

                    if (fullname.isNotEmpty()) {
                        startChat(receiverId, fullname, gamerTag, profileImageUrl, "00","user")
                    } else {
                        Log.e("Chat", "User data is missing fields. Checking organizations...")
                        fetchOrganizationDataAndStartChat(receiverId)
                    }
                } else {
                    // If userData doesn't exist or is empty, check organizationData
                    fetchOrganizationDataAndStartChat(receiverId)
                }
            }.addOnFailureListener {
                Log.e("Chat", "Failed to fetch user data: ${it.message}")
                fetchOrganizationDataAndStartChat(receiverId) // Fallback in case of failure
            }
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


        // Function to start the chat activity
        private fun startChat(
            userId: String,
            fullname: String,
            gamerTag: String,
            profilePicture: String,
            gamerRank: String,
            dataType:String
        ) {
            val intent = Intent(itemView.context, ChatActivity::class.java).apply {
                putExtra("userId", userId)
                putExtra("fullname", fullname)
                putExtra("gamerTag", gamerTag)
                putExtra("profilePicture", profilePicture)
                putExtra("gamerRank", gamerRank)
                putExtra("dataType",dataType)
            }
            itemView.context.startActivity(intent)
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

        val otherUserId = if (chatItem.senderId == FirebaseManager.getCurrentUserId()) {
            chatItem.receiverId
        } else {
            chatItem.senderId
        }

        val database = FirebaseManager.getDatabseInstance()
        val userRef = database.getReference("userData").child(otherUserId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                // Found user in userData
                val profileImageUrl = dataSnapshot.child("profilePicture").value?.toString().orEmpty()
                val fullname = dataSnapshot.child("fullname").value?.toString().orEmpty()

                updateChatUI(holder, fullname, profileImageUrl)
            } else {
                // If not found in userData, check organizationsData
                fetchOrganizationDataAndUpdateUI(holder, otherUserId)
            }
        }.addOnFailureListener {
            fetchOrganizationDataAndUpdateUI(holder, otherUserId) // Fallback in case of failure
        }

        val formattedDate = convertTimestampToDateWithAmPm(chatItem.time)
        holder.timeTextView.text = formattedDate

        holder.newMsgIndicatorTextView.visibility = if (chatItem.time > chatItem.lastReadTime) {
            holder.newMsgIndicatorTextView.text = "new message"
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun fetchOrganizationDataAndUpdateUI(holder: ChatViewHolder, organizationId: String) {
        val database = FirebaseManager.getDatabseInstance()
        val orgRef = database.getReference("organizationsData").child(organizationId)

        orgRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                // Found organization in organizationsData
                val profileImageUrl = dataSnapshot.child("organizationLogo").value?.toString().orEmpty()
                val orgName = dataSnapshot.child("organizationName").value?.toString().orEmpty()

                updateChatUI(holder, orgName, profileImageUrl)
            } else {
                // Neither user nor organization found, set defaults
                updateChatUI(holder, "Unknown User", "")
            }
        }.addOnFailureListener {
            updateChatUI(holder, "Unknown User", "")
        }
    }

    private fun updateChatUI(holder: ChatViewHolder, name: String, profileImageUrl: String) {
        holder.usernameTextView.text = name.ifEmpty { "Unknown User" }

        Glide.with(holder.profileImage.context)
            .load(profileImageUrl.ifEmpty { null })
            .placeholder(R.drawable.game_icon_foreground)
            .error(R.drawable.game_icon_foreground)
            .circleCrop()
            .into(holder.profileImage)
    }

    override fun getItemCount(): Int = chatList.size

    fun updateChatList(newChatList: List<ChatItem>) {
        chatList = newChatList
        notifyDataSetChanged()
    }

    fun convertTimestampToDateWithAmPm(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun showDeleteChatDialog(context: Context, chatItem: ChatItem) {

            AlertDialog.Builder(context)
                .setTitle("Delete Chat")
                .setMessage("Are you sure you want to delete this chat?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteChat(chatItem)
                }
                .setNegativeButton("No", null)
                .show()
    }

    private fun deleteChat(chatItem: ChatItem) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.e("DeleteChat", "User is not authenticated.")
            return
        }

        val chatPath = if (chatItem.senderId == currentUserId) {
            "userData/$currentUserId/chats/${chatItem.receiverId}-${chatItem.senderId}"
        } else if (chatItem.receiverId == currentUserId) {
            "userData/$currentUserId/chats/${chatItem.receiverId}-${chatItem.senderId}"
        } else {
            Log.e("DeleteChat", "User is neither the sender nor the receiver.")
            return
        }

        val database = FirebaseDatabase.getInstance()
        val chatReference = database.getReference(chatPath)

        chatReference.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DeleteChat", "Successfully deleted chat node for chatId: ${chatItem.chatId}")
                    chatList = chatList.filter { it.chatId != chatItem.chatId }
                    notifyDataSetChanged()
                } else {
                    Log.e("DeleteChat", "Failed to delete chat node for chatId: ${chatItem.chatId}", task.exception)
                }
            }
    }


}

