package com.muhaimen.arenax.userProfile

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Post

class PostsAdapter(private val postsList: List<Post>) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    // ViewHolder class
    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.image)           // Image view for post content


        // Function to bind data to views
        fun bind(post: Post) {
            val uri = Uri.parse(post.postContent)

            // Check if the media exists before loading

            Glide.with(itemView.context)
                .load(uri)
                .thumbnail(0.1f) // Show a thumbnail while loading
                .error(R.mipmap.appicon2) // Show a default error image if loading fails
                .into(postImage)
        }
    }


    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.posts_card, parent, false)
        return PostViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postsList[position]
        holder.bind(post)
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return postsList.size
    }
}
