package com.muhaimen.arenax.userProfile

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Story

class HighlightsAdapter(private val storiesList: List<Story>) : RecyclerView.Adapter<HighlightsAdapter.StoryViewHolder>() {

    // ViewHolder class
    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyImage: ImageView = itemView.findViewById(R.id.highlight_image)
        val storyTitle: TextView = itemView.findViewById(R.id.highlight_title)

        // Function to bind data to views
        @SuppressLint("SetTextI18n")
        fun bind(story: Story) {
            val uri = convertToUri(story.mediaUrl)

            // Load the story image using Glide, with a thumbnail preview (no placeholder)
            Glide.with(itemView.context)
                .load(uri)
                .thumbnail(0.1f)
                .error(R.mipmap.appicon2) // Set an error image if loading fails
                .into(storyImage)

            storyTitle.text = "story" // Assuming you want to display the story title
        }
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.highlights_card, parent, false) // Inflate the highlight item layout
        return StoryViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storiesList[position]
        holder.bind(story)
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return storiesList.size
    }

    // Convert mediaUrl to Uri
    private fun convertToUri(mediaUrl: String): Uri? {
        return when {
            mediaUrl.isEmpty() -> null
            mediaUrl.startsWith("content://") -> Uri.parse(mediaUrl) // Handle content URIs
            mediaUrl.startsWith("http://") || mediaUrl.startsWith("https://") -> Uri.parse(mediaUrl) // Handle web URIs
            else -> Uri.parse("file://$mediaUrl") // Fallback for file paths
        }
    }
}
