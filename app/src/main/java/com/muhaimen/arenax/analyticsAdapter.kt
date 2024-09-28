package com.muhaimen.arenax

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class AnalyticsAdapter(private val analyticsList: List<AnalyticsData>) : RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder>() {

    // ViewHolder class
    inner class AnalyticsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameIcon: ImageView = itemView.findViewById(R.id.game_icon)
        val gameName: TextView = itemView.findViewById(R.id.game_name)
        val totalHours: TextView = itemView.findViewById(R.id.total_hours)
        val graphView: GraphView = itemView.findViewById(R.id.graph_view) // Updated to GraphView

        // Function to populate the graph with static data
        fun bind(data: AnalyticsData) {
            gameName.text = data.gameName
            totalHours.text = "Total Hours: ${data.totalHours}"
            gameIcon.setImageResource(data.iconResId) // Assuming iconResId is part of AnalyticsData
            populateGraph(data.hoursData) // hoursData will be a list of DataPoint objects
        }

        // Populate the graph
        private fun populateGraph(hoursData: List<DataPoint>) {
            val series = LineGraphSeries(hoursData.toTypedArray())
            series.color = itemView.context.getColor(android.R.color.holo_blue_dark)
            series.isDrawDataPoints = true
            series.dataPointsRadius = 5f

            // Add the series to the graph view
            graphView.addSeries(series)
            graphView.viewport.isXAxisBoundsManual = true
            graphView.viewport.isYAxisBoundsManual = true

            // Set manual bounds for the viewport
            graphView.viewport.setMinX(0.0)
            graphView.viewport.setMaxX(hoursData.size.toDouble())
            graphView.viewport.setMinY(0.0)
            graphView.viewport.setMaxY(100.0) // Adjust this according to your data

            graphView.gridLabelRenderer.horizontalAxisTitle = "Time"
            graphView.gridLabelRenderer.verticalAxisTitle = "Hours"
        }
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.analytics_card, parent, false)
        return AnalyticsViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: AnalyticsViewHolder, position: Int) {
        val analyticsData = analyticsList[position]
        holder.bind(analyticsData)
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return analyticsList.size
    }
}
