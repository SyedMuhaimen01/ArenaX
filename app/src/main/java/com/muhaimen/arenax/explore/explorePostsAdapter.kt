package com.muhaimen.arenax.userProfile

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.uploadContent.ViewPost

class explorePostsAdapter(private val postsList: List<Post>) : RecyclerView.Adapter<explorePostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.image)
        //private val usernameTextView: TextView = itemView.findViewById(R.id.username)
        //private val captionTextView: TextView = itemView.findViewById(R.id.caption)
        //private val likesTextView: TextView = itemView.findViewById(R.id.likes)
        //private val commentsTextView: TextView = itemView.findViewById(R.id.comments)
        //private val sharesTextView: TextView = itemView.findViewById(R.id.shares)

        fun bind(post: Post) {
            // Bind the post image
            val uri = Uri.parse(post.postContent)
            Glide.with(itemView.context)
                .load(uri)
                .thumbnail(0.1f)
                .error(R.mipmap.appicon2)
                .into(postImage)

            // Set the username and caption
            //usernameTextView.text = post.userFullName
            //captionTextView.text = post.caption ?: "No Caption Provided"
            //likesTextView.text = "${post.likes} Likes"
            //commentsTextView.text = "${post.comments} Comments"
            //sharesTextView.text = "${post.shares} Shares"

            // Set the click listener for navigating to ViewPost activity
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ViewPost::class.java).apply {
                    putExtra("POST", post)
                }
                itemView.context.startActivity(intent) // Start the activity
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
