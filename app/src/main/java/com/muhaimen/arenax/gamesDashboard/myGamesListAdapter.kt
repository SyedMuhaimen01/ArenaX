package com.muhaimen.arenax.gamesDashboard

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.AnalyticsData

class MyGamesListAdapter(private var analyticsList: List<AnalyticsData>) : RecyclerView.Adapter<MyGamesListAdapter.AnalyticsViewHolder>() {

    inner class AnalyticsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameIcon: ImageView = itemView.findViewById(R.id.game_icon)
        val gameName: TextView = itemView.findViewById(R.id.game_name)
        val totalHours: TextView = itemView.findViewById(R.id.total_hours)
        val graphView: GraphView = itemView.findViewById(R.id.line_chart)

        fun bind(data: AnalyticsData) {
            gameName.text = data.gameName
            totalHours.text = "Total Hours: ${data.totalHours}"

            val formattedIcon = formatUrl(data.iconResId)

            Glide.with(itemView.context)
                .load(formattedIcon)
                .placeholder(R.drawable.circle) // Optional placeholder while loading
                .error(R.drawable.back_icon_foreground) // Optional error drawable
                .into(gameIcon)

            populateGraph(data.graphData) // Populate the graph with hours data
        }

        private fun populateGraph(hoursData: List<DataPoint>) {
            val series = LineGraphSeries(hoursData.toTypedArray())
            series.color = itemView.context.getColor(R.color.primaryColor)
            series.isDrawDataPoints = false
            series.dataPointsRadius = 5f

            graphView.removeAllSeries()
            graphView.addSeries(series)

            // Customize grid and axes
            graphView.gridLabelRenderer.isHorizontalLabelsVisible = false
            graphView.gridLabelRenderer.isVerticalLabelsVisible = false
            graphView.gridLabelRenderer.gridColor = Color.TRANSPARENT // Remove grid color
            graphView.gridLabelRenderer.setGridStyle(GridLabelRenderer.GridStyle.BOTH)

            // Configure the viewport
            graphView.viewport.isXAxisBoundsManual = true
            graphView.viewport.isYAxisBoundsManual = true

            val maxX = hoursData.size.toDouble()
            graphView.viewport.setMinX(maxX - 4.0)  // Display the last 4 points
            graphView.viewport.setMaxX(maxX)
            graphView.viewport.setMinY(0.0)
            graphView.viewport.setMaxY(100.0) // Adjust this according to your data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.analytics_card, parent, false)
        return AnalyticsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnalyticsViewHolder, position: Int) {
        val analyticsData = analyticsList[position]
        holder.bind(analyticsData)
    }

    override fun getItemCount(): Int {
        return analyticsList.size
    }

    fun updateGamesList(newList: List<AnalyticsData>) {
        analyticsList = newList
        notifyDataSetChanged()
    }

    fun formatUrl(url: String?): String {
        return when {
            url.isNullOrEmpty() -> "" // Return empty string for null or empty input
            url.startsWith("http://") || url.startsWith("https://") -> url // Return the URL as is
            else -> "https:$url" // Prepend with https if it starts with //
        }
    }

}
