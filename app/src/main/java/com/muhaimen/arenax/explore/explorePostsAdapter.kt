package com.muhaimen.arenax.userProfile

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.uploadContent.ViewPost

data class UserPost(
    val username: String,
    val profilePictureUrl: String,
    val postContent: String,
    val caption: String,
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val trimmedAudioUrl: String?,
    val createdAt: String
)

class explorePostsAdapter(private val postsList: List<UserPost>) : RecyclerView.Adapter<explorePostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.image)
        fun bind(post: UserPost) {
            // Bind the post image
            val uri = Uri.parse(post.postContent)
            Glide.with(itemView.context)
                .load(uri)
                .thumbnail(0.1f)
                .error(R.mipmap.appicon2)
                .into(postImage)

            // Set the click listener for navigating to ViewPost activity
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ViewPost::class.java).apply {
                    putExtra("MEDIA", post.postContent)
                    putExtra("Caption", post.caption)
                    putExtra("Likes", post.likes)
                    putExtra("Comments", post.comments)
                    putExtra("Shares", post.shares)
                    putExtra("trimAudio", post.trimmedAudioUrl)
                    putExtra("createdAt", post.createdAt)
                    putExtra("Username", post.username)
                    putExtra("ProfilePicture", post.profilePictureUrl)
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.posts_card, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postsList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return postsList.size
    }
}
