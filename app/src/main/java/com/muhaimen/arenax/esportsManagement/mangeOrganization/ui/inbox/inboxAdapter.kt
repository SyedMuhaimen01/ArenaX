package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.inbox

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.inbox.Threads.organizationChatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class inboxAdapter(
    private var chatList: MutableList<ChatItem>,
    private val organizationId: String?
) : RecyclerView.Adapter<inboxAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var profileImage: ImageView = view.findViewById(R.id.profilePicture)
        var usernameTextView: TextView = view.findViewById(R.id.fullname)
        var timeTextView: TextView = view.findViewById(R.id.time)
        var newMsgIndicatorTextView: TextView = view.findViewById(R.id.newMsgIndicator)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chatItem = chatList[position]
                    val otherUserId =
                        if (chatItem.senderId == organizationId) chatItem.receiverId else chatItem.senderId
                    fetchUserDataAndStartChat(otherUserId)
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteChatDialog(itemView.context, chatList[position])
                }
                true
            }
        }

        private fun fetchUserDataAndStartChat(userId: String) {

            FirebaseDatabase.getInstance().getReference("userData").child(userId).get()
                .addOnSuccessListener { dataSnapshot: DataSnapshot ->
                    if (dataSnapshot.exists()) {
                        val profileImageUrl = dataSnapshot.child("profilePicture").getValue(String::class.java)
                        val fullname = dataSnapshot.child("fullname").getValue(String::class.java)
                        val gamerTag = dataSnapshot.child("gamerTag").getValue(String::class.java)

                        val intent = Intent(itemView.context, organizationChatActivity::class.java).apply {
                            putExtra("userId", userId)
                            putExtra("fullname", fullname)
                            putExtra("gamerTag", gamerTag)
                            putExtra("profilePicture", profileImageUrl)
                            putExtra("organizationId", organizationId)
                        }
                        itemView.context.startActivity(intent)
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_all_chats_card, parent, false)
        return ChatViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = chatList[position]
        val otherUserId =
            if (chatItem.senderId == organizationId) chatItem.receiverId else chatItem.senderId

        FirebaseDatabase.getInstance().getReference("userData").child(otherUserId).get()
            .addOnSuccessListener { dataSnapshot: DataSnapshot ->
                if (dataSnapshot.exists()) {
                    holder.usernameTextView.text = dataSnapshot.child("fullname").getValue(String::class.java)
                    val profileImageUrl = dataSnapshot.child("profilePicture").getValue(String::class.java)
                    Glide.with(holder.profileImage.context)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.game_icon_foreground)
                        .error(R.drawable.game_icon_foreground)
                        .circleCrop()
                        .into(holder.profileImage)
                }
            }

        holder.timeTextView.text = convertTimestampToDateWithAmPm(chatItem.time)
        holder.newMsgIndicatorTextView.visibility =
            if (chatItem.time > chatItem.lastReadTime) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    fun updateChatList(newChatList: List<ChatItem>) {
        this.chatList = newChatList.toMutableList()
        notifyDataSetChanged()
    }

    private fun convertTimestampToDateWithAmPm(timestamp: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    private fun showDeleteChatDialog(context: Context, chatItem: ChatItem) {
        AlertDialog.Builder(context)
            .setTitle("Delete Chat")
            .setMessage("Are you sure you want to delete this chat?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                deleteChat(chatItem)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteChat(chatItem: ChatItem) {
        if (organizationId == null) {
            Log.e("DeleteChat", "Organization ID is null.")
            return
        }

        val chatPath = "organizationData/$organizationId/chats/${chatItem.receiverId}-${chatItem.senderId}"
        FirebaseDatabase.getInstance().getReference(chatPath).removeValue()
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Log.d("DeleteChat", "Successfully deleted chat: ${chatItem.chatId}")
                    chatList.remove(chatItem)
                    notifyDataSetChanged()
                } else {
                    Log.e("DeleteChat", "Failed to delete chat", task.exception)
                }
            }
    }
}
