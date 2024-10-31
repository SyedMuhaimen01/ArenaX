package com.muhaimen.arenax.Threads

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem
import com.muhaimen.arenax.utils.FirebaseManager
import android.app.AlertDialog
import android.content.Context
import android.util.Log

class ChatsAdapter(private val chatMessages: MutableList<ChatItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val VIEW_TYPE_TEXT_SENT = 1
        const val VIEW_TYPE_TEXT_RECEIVED = 2
        const val VIEW_TYPE_IMAGE_SENT = 3
        const val VIEW_TYPE_IMAGE_RECEIVED = 4
        const val VIEW_TYPE_VIDEO_SENT = 5
        const val VIEW_TYPE_VIDEO_RECEIVED = 6
    }

    override fun getItemViewType(position: Int): Int {
        val chatItem = chatMessages[position]
        return when {
            chatItem.senderId == FirebaseManager.getCurrentUserId() -> {
                when (chatItem.contentType) {
                    ChatItem.ContentType.TEXT -> VIEW_TYPE_TEXT_SENT
                    ChatItem.ContentType.IMAGE -> VIEW_TYPE_IMAGE_SENT
                    ChatItem.ContentType.VIDEO -> VIEW_TYPE_VIDEO_SENT
                    else -> VIEW_TYPE_TEXT_SENT
                }
            }
            else -> {
                when (chatItem.contentType) {
                    ChatItem.ContentType.TEXT -> VIEW_TYPE_TEXT_RECEIVED
                    ChatItem.ContentType.IMAGE -> VIEW_TYPE_IMAGE_RECEIVED
                    ChatItem.ContentType.VIDEO -> VIEW_TYPE_VIDEO_RECEIVED
                    else -> VIEW_TYPE_TEXT_RECEIVED
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_msg_right, parent, false)
                SentTextViewHolder(view)
            }
            VIEW_TYPE_TEXT_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_msg_left, parent, false)
                ReceivedTextViewHolder(view)
            }
            VIEW_TYPE_IMAGE_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_img_right, parent, false)
                SentImageViewHolder(view)
            }
            VIEW_TYPE_IMAGE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_img_left, parent, false)
                ReceivedImageViewHolder(view)
            }
            VIEW_TYPE_VIDEO_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_video_right, parent, false)
                SentVideoViewHolder(view)
            }
            VIEW_TYPE_VIDEO_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_video_left, parent, false)
                ReceivedVideoViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatItem = chatMessages[position]

        when (holder) {
            is SentTextViewHolder -> holder.bind(chatItem.message,chatItem.formattedSentTime) { showUnsendDialog(holder.itemView.context, chatItem) }
            is ReceivedTextViewHolder -> holder.bind(chatItem.message,chatItem.formattedSentTime) { showUnsendDialog(holder.itemView.context, chatItem) }
            is SentImageViewHolder -> holder.bind(chatItem.contentUri,chatItem.formattedSentTime) { showUnsendDialog(holder.itemView.context, chatItem) }
            is ReceivedImageViewHolder -> holder.bind(chatItem.contentUri,chatItem.formattedSentTime) { showUnsendDialog(holder.itemView.context, chatItem) }
            is SentVideoViewHolder -> holder.bind(chatItem.contentUri,chatItem.formattedSentTime) { showUnsendDialog(holder.itemView.context, chatItem) }
            is ReceivedVideoViewHolder -> holder.bind(chatItem.contentUri,chatItem.formattedSentTime) { showUnsendDialog(holder.itemView.context, chatItem) }
        }
    }

    override fun getItemCount(): Int = chatMessages.size

    inner class SentTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewMessage: TextView = view.findViewById(R.id.sentMessageTextView)
        private val time: TextView = view.findViewById(R.id.time)
        fun bind(message: String?, formattedSentTime: String?, onLongPress: () -> Unit) {
            time.text = formattedSentTime
            textViewMessage.text = message
            itemView.setOnLongClickListener {
                onLongPress.invoke()
                true
            }
        }
    }

    inner class ReceivedTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewMessage: TextView = view.findViewById(R.id.receivedMessageTextView)
        private val time: TextView = view.findViewById(R.id.time)
        fun bind(message: String?,formattedSentTime: String, onLongPress: () -> Unit) {
            time.text = formattedSentTime
            textViewMessage.text = message
            itemView.setOnLongClickListener {
                onLongPress.invoke()
                true
            }
        }
    }

    inner class SentImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageViewMessage: ImageView = view.findViewById(R.id.imageView)
        private val time: TextView = view.findViewById(R.id.time)
        fun bind(imageUrl: String?, formattedSentTime: String, onLongPress: () -> Unit) {
            time.text = formattedSentTime
            Glide.with(itemView.context).load(imageUrl).into(imageViewMessage)
            imageViewMessage.setOnClickListener {
                val intent = Intent(itemView.context, viewChatMedia::class.java).apply {
                    putExtra("mediaUrl", imageUrl)
                    putExtra("mediaType", "image")
                }
                itemView.context.startActivity(intent)
            }
            itemView.setOnLongClickListener {
                onLongPress.invoke()
                true
            }
        }
    }

    inner class ReceivedImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageViewMessage: ImageView = view.findViewById(R.id.imageView)
        private val time: TextView = view.findViewById(R.id.time)
        fun bind(imageUrl: String?, formattedSentTime: String, onLongPress: () -> Unit) {
            time.text = formattedSentTime
            Glide.with(itemView.context).load(imageUrl).into(imageViewMessage)
            imageViewMessage.setOnClickListener {
                val intent = Intent(itemView.context, viewChatMedia::class.java).apply {
                    putExtra("mediaUrl", imageUrl)
                    putExtra("mediaType", "image")
                }
                itemView.context.startActivity(intent)
            }
            itemView.setOnLongClickListener {
                onLongPress.invoke()
                true
            }
        }
    }

    inner class SentVideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val videoViewMessage: VideoView = view.findViewById(R.id.VideoView)
        private val thumbnailView: ImageView = view.findViewById(R.id.videoThumbnail)
        private val time: TextView = view.findViewById(R.id.time)
        fun bind(videoUrl: String?, formattedSentTime: String, onLongPress: () -> Unit) {
            time.text = formattedSentTime
            videoViewMessage.setVideoURI(Uri.parse(videoUrl))
            loadVideoThumbnail(videoUrl, thumbnailView)

            thumbnailView.setOnClickListener {
                navigateToViewChatMedia(videoUrl, "video")
            }

            videoViewMessage.setOnClickListener {
                navigateToViewChatMedia(videoUrl, "video")
            }

            videoViewMessage.setOnCompletionListener {
                thumbnailView.visibility = View.VISIBLE
            }

            itemView.setOnLongClickListener {
                onLongPress.invoke()
                true
            }
        }

        private fun loadVideoThumbnail(videoUrl: String?, imageView: ImageView) {
            videoUrl?.let {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(it, HashMap())
                val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                }
                retriever.release()
            }
        }

        private fun navigateToViewChatMedia(mediaUrl: String?, mediaType: String) {
            val intent = Intent(itemView.context, viewChatMedia::class.java).apply {
                putExtra("mediaUrl", mediaUrl)
                putExtra("mediaType", mediaType)
            }
            itemView.context.startActivity(intent)
        }
    }

    inner class ReceivedVideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val videoViewMessage: VideoView = view.findViewById(R.id.VideoView)
        private val thumbnailView: ImageView = view.findViewById(R.id.videoThumbnail)
        private val time: TextView = view.findViewById(R.id.time)
        fun bind(videoUrl: String?, formattedSentTime: String, onLongPress: () -> Unit) {
            time.text = formattedSentTime
            videoViewMessage.setVideoURI(Uri.parse(videoUrl))
            loadVideoThumbnail(videoUrl, thumbnailView)

            thumbnailView.setOnClickListener {
                navigateToViewChatMedia(videoUrl, "video")
            }

            videoViewMessage.setOnClickListener {
                navigateToViewChatMedia(videoUrl, "video")
            }

            videoViewMessage.setOnCompletionListener {
                thumbnailView.visibility = View.VISIBLE
            }

            itemView.setOnLongClickListener {
                onLongPress.invoke()
                true
            }
        }

        private fun loadVideoThumbnail(videoUrl: String?, imageView: ImageView) {
            videoUrl?.let {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(it, HashMap())
                val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                }
                retriever.release()
            }
        }

        private fun navigateToViewChatMedia(mediaUrl: String?, mediaType: String) {
            val intent = Intent(itemView.context, viewChatMedia::class.java).apply {
                putExtra("mediaUrl", mediaUrl)
                putExtra("mediaType", mediaType)
            }
            itemView.context.startActivity(intent)
        }
    }

    private fun showUnsendDialog(context: Context, chatItem: ChatItem) {
        // Check if the current user is the sender of the message
        if (chatItem.senderId == FirebaseManager.getCurrentUserId()) {
            AlertDialog.Builder(context)
                .setTitle("Unsend Message")
                .setMessage("Are you sure you want to unsend this message?")
                .setPositiveButton("Yes") { _, _ ->
                    unsendMessage(chatItem)
                }
                .setNegativeButton("No", null)
                .show()
        } else {

        }
    }



    private fun unsendMessage(chatItem: ChatItem) {
        // Log the function call
        Log.d("UnsendMessage", "Called to unsend message with chatId: ${chatItem.chatId}")

        // Get a reference to the "chats" node
        val chatsReference = FirebaseDatabase.getInstance().getReference("chats")

        // Query the "chats" node to find the specific chat node using the chatId
        chatsReference.orderByChild("chatId").equalTo(chatItem.chatId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UnsendMessage", "Chat node found for chatId: ${chatItem.chatId}")

                // Check if any matching chat node is found
                for (snapshot in task.result.children) {
                    Log.d("UnsendMessage", "Found matching chat node: ${snapshot.key}")

                    // Remove the entire chat node
                    snapshot.ref.removeValue().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Log.d("UnsendMessage", "Successfully removed chat node with chatId: ${chatItem.chatId}")
                            // Successfully removed the message
                            chatMessages.remove(chatItem)
                            notifyDataSetChanged() // Notify the adapter of data change
                        } else {
                            // Handle failure (e.g., log an error)
                            Log.e("UnsendMessage", "Failed to remove chat node with chatId: ${chatItem.chatId}", deleteTask.exception)
                        }
                    }
                }
            } else {
                // Handle failure to retrieve chats (e.g., log an error)
                Log.e("UnsendMessage", "Failed to retrieve chats for chatId: ${chatItem.chatId}", task.exception)
            }
        }
    }



}
