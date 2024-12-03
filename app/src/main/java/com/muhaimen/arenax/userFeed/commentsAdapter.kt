package com.muhaimen.arenax.userFeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R

class CommentsAdapter(
    private val commentsList: MutableList<String>  // List of comments
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    // ViewHolder to handle displaying comments
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentText: TextView = itemView.findViewById(R.id.commentTextView)
        val commentProfilePicture: ImageView = itemView.findViewById(R.id.commentProfilePicture)
    }

    override fun getItemCount(): Int {
        return commentsList.size // Only the number of comments
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        // Inflate the simplified comment layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.userfeed_comments_card, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        // Bind the comment text
        val comment = commentsList[position]
        holder.commentText.text = comment
        holder.commentProfilePicture.setImageResource(R.drawable.game_icon_foreground)
    }
}
