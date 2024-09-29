package com.muhaimen.arenax.userProfile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R

class HighlightsAdapter(private val highlightsList: List<Highlight>) : RecyclerView.Adapter<HighlightsAdapter.HighlightViewHolder>() {

    // ViewHolder class
    inner class HighlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val highlightImage: ImageView = itemView.findViewById(R.id.highlight_image)
        val highlightTitle: TextView = itemView.findViewById(R.id.highlight_title)

        // Function to bind data to views
        fun bind(highlight: Highlight) {
            highlightImage.setImageResource(highlight.imageResId)
            highlightTitle.text = highlight.title
        }
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.highlights_card, parent, false) // Inflate the highlight item layout
        return HighlightViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: HighlightViewHolder, position: Int) {
        val highlight = highlightsList[position]
        holder.bind(highlight)
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return highlightsList.size
    }
}
