package com.muhaimen.arenax.userProfile

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.uploadContent.ViewPost
import com.muhaimen.arenax.uploadStory.viewStory

class PostsAdapter(private val postsList: List<Post>) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.image)

        fun bind(post: Post) {

            itemView.setOnClickListener {

                val intent = Intent(itemView.context, ViewPost::class.java).apply {
                    putExtra("MEDIA", post.postContent)
                    putExtra("Caption", post.caption)
                    putExtra("Likes", post.likes)
                    putExtra("Comments", post.comments)
                    putExtra("Shares", post.shares)
                    putExtra("trimAudio", post.trimmedAudioUrl)
                    putExtra("createdAt", post.createdAt)
                }
                itemView.context.startActivity(intent) // Start the activity
            }
            // Check if the media exists before loading
            val uri = Uri.parse(post.postContent)
            Glide.with(itemView.context)
                .load(uri)
                .thumbnail(0.1f)
                .error(R.mipmap.appicon2)
                .into(postImage)
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
