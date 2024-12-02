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

data class DummyPost(
    val profilePictureUrl: String,
    val userName: String,
    val contentType: String,
    val contentText: String? = null, // for text post
    val contentImageUrl: String? = null, // for image post
    val contentVideoUrl: String? = null, // for video post
    val comments: MutableList<String> = mutableListOf() // list of comments
)

class UserFeedPostsAdapter(
    private val dummyPosts: List<DummyPost>,
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
        private val tvContentText: TextView = itemView.findViewById(R.id.tvContentText)
        private val tvCommentsCount: TextView = itemView.findViewById(R.id.commentCount)
        private val btnLike: ImageButton = itemView.findViewById(R.id.likeButton)
        private val btnComment: ImageButton = itemView.findViewById(R.id.commentButton)
        private val btnShare: ImageButton = itemView.findViewById(R.id.shareButton)
        private val recyclerViewComments: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)

        fun bind(post: DummyPost) {
            Glide.with(itemView.context)
                .load(post.profilePictureUrl)
                .placeholder(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(imgProfilePicture)

            tvUserName.text = post.userName
            tvContentText.text = post.contentText
            tvCommentsCount.text = post.comments.size.toString()

            // Handle comment section by passing comments to the CommentsAdapter
            val commentAdapter = CommentsAdapter(post.comments) { newComment ->
                // Handle new comment posted, or notify the parent to update the data
                onCommentClick(post)
            }
            recyclerViewComments.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(itemView.context)
            recyclerViewComments.adapter = commentAdapter

            // Handle Like, Comment, Share button clicks
            btnLike.setOnClickListener {
                onLikeClick(post)
            }

            btnComment.setOnClickListener {
                onCommentClick(post)
            }

            btnShare.setOnClickListener {
                onShareClick(post)
            }
        }
    }
}
