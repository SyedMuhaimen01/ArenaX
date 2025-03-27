
package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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

class MyGamesListAdapter(private var analyticsList: List<AnalyticsData>, private val userId: String) : RecyclerView.Adapter<MyGamesListAdapter.AnalyticsViewHolder>() {

    inner class AnalyticsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameIcon: ImageView = itemView.findViewById(R.id.game_icon)
        private val gameName: TextView = itemView.findViewById(R.id.game_name)
        val totalHours: TextView = itemView.findViewById(R.id.total_hours)
        private val graphView: GraphView = itemView.findViewById(R.id.line_chart)

        @SuppressLint("SetTextI18n")
        fun bind(data: AnalyticsData) {
            gameName.text = data.gameName
            totalHours.text = "Total Hours: ${data.totalHours}"

            val formattedIcon = formatUrl(data.iconResId)
            Glide.with(itemView.context)
                .load(formattedIcon)
                .placeholder(R.drawable.circle)
                .error(R.drawable.back_icon_foreground)
                .into(gameIcon)

            populateGraph(data.graphData)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ViewGameAnalytics::class.java).apply {
                    putExtra("userId", userId)
                    putExtra("gameName", data.gameName)
                }
                itemView.context.startActivity(intent)
            }
        }

        private fun populateGraph(graphData: List<Pair<String, Double>>) {
            // Convert the List<Pair<String, Double>> to DataPoint[]
            val dataPoints = graphData.mapIndexed { index, pair ->
                DataPoint(index.toDouble(), pair.second) // index for X-axis and total hours for Y-axis
            }.toTypedArray()

            val series = LineGraphSeries(dataPoints)
            series.color = itemView.context.getColor(R.color.interactiveColor)
            series.isDrawDataPoints = false
            series.dataPointsRadius = 5f

            // Clear previous series
            graphView.removeAllSeries()

            // Add the series to the graph view
            graphView.addSeries(series)

            // Customize grid and axes
            graphView.gridLabelRenderer.isHorizontalLabelsVisible = false
            graphView.gridLabelRenderer.isVerticalLabelsVisible = false
            graphView.gridLabelRenderer.gridColor = Color.TRANSPARENT // Remove grid color

            // Show only X and Y axes
            graphView.gridLabelRenderer.setGridStyle(GridLabelRenderer.GridStyle.BOTH) // Show both axes
            graphView.gridLabelRenderer.horizontalAxisTitle = "" // Remove horizontal axis title
            graphView.gridLabelRenderer.verticalAxisTitle = "" // Remove vertical axis title

            // Configure the viewport
            graphView.viewport.isXAxisBoundsManual = true
            graphView.viewport.isYAxisBoundsManual = true

            // Set manual bounds for the viewport
            val maxX = dataPoints.size.toDouble()
            graphView.viewport.setMinX(maxX - 4.0)  // Display the last 4 points
            graphView.viewport.setMaxX(maxX)
            graphView.viewport.setMinY(0.0)
            graphView.viewport.setMaxY(dataPoints.maxOfOrNull { it.y } ?: 100.0) // Adjust according to your data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mygames_list_card, parent, false)
        return AnalyticsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnalyticsViewHolder, position: Int) {
        val analyticsData = analyticsList[position]
        holder.bind(analyticsData)
    }

    override fun getItemCount(): Int {
        return analyticsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateGamesList(newList: List<AnalyticsData>) {
        analyticsList = newList
        notifyDataSetChanged()
    }

    private fun formatUrl(url: String?): String {
        return when {
            url.isNullOrEmpty() -> ""
            url.startsWith("http://") || url.startsWith("https://") -> url
            else -> "https:$url"
        }
    }
}
