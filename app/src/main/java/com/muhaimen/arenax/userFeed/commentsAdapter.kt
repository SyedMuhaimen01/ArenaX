package com.muhaimen.arenax.userFeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment

class commentsAdapter(
    private val commentsList: List<Comment> // List of Comment objects
) : RecyclerView.Adapter<commentsAdapter.CommentViewHolder>() {

    // ViewHolder to handle displaying comments
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentText: TextView = itemView.findViewById(R.id.commentTextView)
        val commenterName: TextView = itemView.findViewById(R.id.commenterNameTextView)
        val commentProfilePicture: ImageView = itemView.findViewById(R.id.commentProfilePicture)
        val commentTimestamp: TextView = itemView.findViewById(R.id.commentTimestampTextView)
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        // Inflate the layout for a comment
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.userfeed_comments_card, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        // Bind the comment data
        val comment = commentsList[position]

        // Set commenter's name
        holder.commenterName.text = comment.commenterName

        // Set the comment text
        holder.commentText.text = comment.commentText

        // Load profile picture using Glide, with a placeholder if null
        Glide.with(holder.itemView.context)
            .load(comment.commenterProfilePictureUrl)
            .placeholder(R.drawable.game_icon_foreground)
            .circleCrop()
            .into(holder.commentProfilePicture)

        // Display comment timestamp
        holder.commentTimestamp.text = comment.createdAt
    }
}
