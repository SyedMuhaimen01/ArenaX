package com.muhaimen.arenax.userFeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R

class CommentsAdapter(
    private val commentsList: MutableList<String>,  // List of comments
    private val onPostCommentListener: (String) -> Unit  // Callback for posting a new comment
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val VIEW_TYPE_COMMENT = 1
    private val VIEW_TYPE_INPUT = 2
    private var visibleCommentCount = 1 // Initially show 1 comment

    // ViewHolder to handle both comments and input field
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentText: TextView = itemView.findViewById(R.id.textViewComment)
        val seeMoreLink: TextView = itemView.findViewById(R.id.seeMoreLink)
        val commentEditText: EditText = itemView.findViewById(R.id.editTextComment)
        val postButton: Button = itemView.findViewById(R.id.buttonPostComment)
    }

    override fun getItemCount(): Int {
        // Visible comments + 1 input field
        return visibleCommentCount + 1
    }

    override fun getItemViewType(position: Int): Int {
        // Last item is for the input field
        return if (position == visibleCommentCount) VIEW_TYPE_INPUT else VIEW_TYPE_COMMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.userfeed_comments_card, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_COMMENT -> {
                // Show comment-related UI
                holder.commentText.visibility = View.VISIBLE
                holder.seeMoreLink.visibility = if (position == visibleCommentCount - 1 && visibleCommentCount < commentsList.size) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                holder.commentEditText.visibility = View.GONE
                holder.postButton.visibility = View.GONE

                // Bind comment text
                holder.commentText.text = commentsList[position]

                // "See More" functionality
                holder.seeMoreLink.setOnClickListener {
                    visibleCommentCount = commentsList.size // Show all comments
                    notifyDataSetChanged()
                }
            }

            VIEW_TYPE_INPUT -> {
                // Show input field-related UI
                holder.commentText.visibility = View.GONE
                holder.seeMoreLink.visibility = View.GONE
                holder.commentEditText.visibility = View.VISIBLE
                holder.postButton.visibility = View.VISIBLE

                // Handle comment posting
                holder.postButton.setOnClickListener {
                    val newComment = holder.commentEditText.text.toString().trim()
                    if (newComment.isNotEmpty()) {
                        onPostCommentListener(newComment) // Notify listener
                        addComment(newComment) // Add comment locally
                        holder.commentEditText.text.clear() // Clear input
                    }
                }
            }
        }
    }

    private fun addComment(comment: String) {
        commentsList.add(0, comment) // Add new comment at the top
        visibleCommentCount++ // Update visible count
        notifyDataSetChanged() // Refresh the RecyclerView
    }
}
