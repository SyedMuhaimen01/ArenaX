package com.muhaimen.arenax.userFeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R

// DummyPost class
data class DummyPost(
    val profilePictureUrl: String,
    val userName: String,
    val contentType: String,
    val contentText: String? = null,
    val contentImageUrl: String? = null
)

class UserFeedPostsAdapter(
    private val dummyPosts: List<DummyPost>, // Use DummyPost directly
    private val onLikeClick: (DummyPost) -> Unit,
    private val onCommentClick: (DummyPost) -> Unit,
    private val onShareClick: (DummyPost) -> Unit
) : RecyclerView.Adapter<UserFeedPostsAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.userfeed_posts_card, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = dummyPosts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = dummyPosts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfilePicture: ImageView = itemView.findViewById(R.id.ProfilePicture)
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val imgContent: ImageView = itemView.findViewById(R.id.imgContent)
        private val tvContentText: TextView = itemView.findViewById(R.id.tvContentText)
        private val btnLike: ImageButton = itemView.findViewById(R.id.likeButton)
        private val btnComment: ImageButton = itemView.findViewById(R.id.commentButton)
        private val btnShare: ImageButton = itemView.findViewById(R.id.shareButton)

        fun bind(post: DummyPost) {
            // Bind the user's profile picture using Glide
            Glide.with(itemView.context)
                .load(post.profilePictureUrl)
                .placeholder(R.drawable.game_icon_foreground)
                .circleCrop() // Optional, if you want the profile picture to be circular
                .into(imgProfilePicture)

            // Bind the user's name
            tvUserName.text = post.userName

            // Determine if the content is text or an image and set visibility accordingly
            if (post.contentType == "text") {
                tvContentText.text = post.contentText
                tvContentText.visibility = View.VISIBLE
                imgContent.visibility = View.GONE
            } else if (post.contentType == "image") {
                // Load the content image with Glide
                Glide.with(itemView.context)
                    .load(post.contentImageUrl)
                    .placeholder(R.drawable.game_icon_foreground)
                    .centerCrop() // Optional, for better cropping of the content image
                    .into(imgContent)
                tvContentText.visibility = View.GONE
                imgContent.visibility = View.VISIBLE
            }

            // Handle click events for like, comment, and share
            btnLike.setOnClickListener { onLikeClick(post) }
            btnComment.setOnClickListener { onCommentClick(post) }
            btnShare.setOnClickListener { onShareClick(post) }
        }
    }
}
