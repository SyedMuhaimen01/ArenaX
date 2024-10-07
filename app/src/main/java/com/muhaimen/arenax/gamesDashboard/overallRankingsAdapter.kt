package com.muhaimen.arenax.overallLeaderboardAdapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.AnalyticsData
import com.muhaimen.arenax.dataClasses.RankingData
import com.muhaimen.arenax.gamesDashboard.overallLeaderboard

class overallLeaderboardAdapter(private val rankingsList: List<RankingData>) : RecyclerView.Adapter<overallLeaderboardAdapter.overallLeaderboardViewHolder>() {

    // ViewHolder class
    inner class overallLeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        val name: TextView = itemView.findViewById(R.id.nameTextView)
        val totalHours: TextView = itemView.findViewById(R.id.totalHours)
        val rank: TextView = itemView.findViewById(R.id.rankNumber)
        val gamerTag: TextView = itemView.findViewById(R.id.gamerTagTextView)

        // Function to populate the graph with data
        fun bind(data: RankingData) {
            name.text = data.name
            totalHours.text = "Total Hours: ${data.totalHrs}"
            profilePicture.setImageResource(data.profilePicture)
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


            when (position) {
                0 -> { // Gold
                    holder.itemView.setBackgroundColor(Color.parseColor("#FFD700"))
                    holder.name.setTextColor(Color.WHITE) // Set text color to black
                    holder.totalHours.setTextColor(Color.WHITE) // Set text color to black
                    holder.gamerTag.setTextColor(Color.WHITE)

                }
                1 -> { // Silver
                    holder.itemView.setBackgroundColor(Color.parseColor("#C0C0C0"))
                    holder.name.setTextColor(Color.WHITE) // Set text color to black
                    holder.totalHours.setTextColor(Color.WHITE)
                    holder.gamerTag.setTextColor(Color.WHITE)
                }
                2 -> { // Bronze
                    holder.itemView.setBackgroundColor(Color.parseColor("#cd7f32"))
                    holder.name.setTextColor(Color.WHITE) // Set text color to black
                    holder.totalHours.setTextColor(Color.WHITE)
                    holder.gamerTag.setTextColor(Color.WHITE)
                }
                else -> {
                    holder.itemView.setBackgroundColor(Color.WHITE) // Default background
                }
        }
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return rankingsList.size
    }
}
