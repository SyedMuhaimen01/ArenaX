package com.muhaimen.arenax.userProfile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R

class PostsAdapter(private val postsList: List<Post>) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    // ViewHolder class
    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.image)

        // Function to bind data to views
        fun bind(post: Post) {
            postImage.setImageResource(post.imageResId)
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
