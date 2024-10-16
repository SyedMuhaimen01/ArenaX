package com.muhaimen.arenax.overallLeaderboardAdapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.RankingData

class overallLeaderboardAdapter(val rankingsList: List<RankingData>) : RecyclerView.Adapter<overallLeaderboardAdapter.overallLeaderboardViewHolder>() {

    // ViewHolder class
    inner class overallLeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        val name: TextView = itemView.findViewById(R.id.nameTextView)
        val totalHours: TextView = itemView.findViewById(R.id.totalHours)
        val rank: TextView = itemView.findViewById(R.id.rankNumber)
        val gamerTag: TextView = itemView.findViewById(R.id.gamerTagTextView)

        // Function to populate the view with data
        @SuppressLint("SetTextI18n")
        fun bind(data: RankingData) {
            name.text = data.name
            totalHours.text = "Total Hours: ${data.totalHrs}"


            Log.e("Leaderboard profilepicture", data.profilePicture)
            val picture=formatUrl(data.profilePicture)
            val uri = Uri.parse(data.profilePicture)
            Log.e("Leaderboard", picture)

            Glide.with(itemView.context)
                .load(uri)
                .placeholder(R.drawable.circle)
                .error(R.drawable.circle)
                .circleCrop()
                .into(profilePicture)

            rank.text = data.rank.toString()
            gamerTag.text = data.gamerTag
        }
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): overallLeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rankings_card, parent, false)
        return overallLeaderboardViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: overallLeaderboardViewHolder, position: Int) {
        val rankingsData = rankingsList[position]
        holder.bind(rankingsData)

        // Change background color based on ranking position
        when (position) {
            0 -> { // Gold
                holder.itemView.setBackgroundColor(Color.parseColor("#FFD700"))
                holder.name.setTextColor(Color.WHITE)
                holder.totalHours.setTextColor(Color.WHITE)
                holder.gamerTag.setTextColor(Color.WHITE)
            }
            1 -> { // Silver
                holder.itemView.setBackgroundColor(Color.parseColor("#C0C0C0"))
                holder.name.setTextColor(Color.WHITE)
                holder.totalHours.setTextColor(Color.WHITE)
                holder.gamerTag.setTextColor(Color.WHITE)
            }
            2 -> { // Bronze
                holder.itemView.setBackgroundColor(Color.parseColor("#cd7f32"))
                holder.name.setTextColor(Color.WHITE)
                holder.totalHours.setTextColor(Color.WHITE)
                holder.gamerTag.setTextColor(Color.WHITE)
            }
            else -> {
                holder.itemView.setBackgroundColor(Color.WHITE) // Default background
            }
        }
    }
    fun formatUrl(url: String?): String {
        return when {
            url.isNullOrEmpty() -> "" // Return empty string for null or empty input
            url.startsWith("http://") || url.startsWith("https://") -> url // Return the URL as is
            else -> "https:$url" // Prepend with https if it starts with //
        }
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return rankingsList.size
    }
}
