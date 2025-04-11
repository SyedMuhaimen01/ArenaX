package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.muhaimen.arenax.R
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
class ViewGameAnalytics : AppCompatActivity() {
    private lateinit var gameName: TextView
    private lateinit var totalHours: TextView
    private lateinit var gameIcon: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var totalPlaytimeLineChart: LineChart
    private lateinit var averageSessionLengthLineChart: LineChart
    private lateinit var sessionFrequencyBarChart: BarChart
    private lateinit var peakPlayTimeScatterChart: ScatterChart
    private lateinit var gameComparisonPieChart: PieChart
    private lateinit var auth: FirebaseAuth

    private val TAG = "ViewGameAnalytics"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_game_analytics)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        gameName = findViewById(R.id.game_name)
        totalHours = findViewById(R.id.total_hours)
        gameIcon = findViewById(R.id.game_icon)
        totalPlaytimeLineChart = findViewById(R.id.screenTimeLineChart)
        averageSessionLengthLineChart = findViewById(R.id.averageSessionLengthChart)
        sessionFrequencyBarChart = findViewById(R.id.sessionFrequencyBarChart)
        peakPlayTimeScatterChart = findViewById(R.id.peakPlayTimeChart)
        gameComparisonPieChart = findViewById(R.id.gameComparisonPieChart)
        backButton = findViewById(R.id.backButton)

        val intent = intent
        val game = intent.getStringExtra("gameName")
        if (game != null) {
            gameName.text = game
            fetchUserGameStats(game)
        } else {
            gameName.text = "Unknown Game"
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserGameStats(game: String) {
        val url = "${Constants.SERVER_URL}analytics/gameAnalytics"
        val userId = intent.getStringExtra("userId")
        val requestBody = JSONObject().apply {
            put("gameName", game)
            put("userId", userId)
        }

        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                Log.d(TAG, "Response received: $response")
                parseAndPopulateCharts(response)
            },
            { error ->
                Log.e(TAG, "Error fetching data: ${error.message}")
                error.printStackTrace()
            })

        queue.add(jsonObjectRequest)
    }

    private fun convertToUrl(url: String): String {
        return when {
            url.isEmpty() -> {
                Log.e(TAG, "URL is null or empty!")
                ""
            }
            url.startsWith("http://") || url.startsWith("https://") -> url
            else -> "https:$url"
        }
    }

    private fun parseAndPopulateCharts(response: JSONObject) {
        val game = response.optString("gameName")
        gameName.text = game
        totalHours.text = response.optString("totalPlaytimeCurrentGame")

        val gameLogo = response.optString("logoUrl")
        val logoUrl = convertToUrl(gameLogo)

        Glide.with(this)
            .load(logoUrl)
            .placeholder(R.drawable.circle)
            .error(R.drawable.circle)
            .circleCrop()
            .into(gameIcon)

        val dailyStats = response.optJSONArray("dailyStats") ?: return
        val playtimePerDay = mutableListOf<Float>()
        val avgSessionLengthPerDay = mutableListOf<Float>()
        val sessionFrequencyPerDay = mutableListOf<Int>()
        val peakPlaytimePerDay = mutableListOf<Float>()
        val dates = mutableListOf<String>()

        for (i in 0 until dailyStats.length()) {
            val dayData = dailyStats.optJSONObject(i) ?: continue
            playtimePerDay.add(dayData.optDouble("totalPlaytime", 0.0).toFloat())
            avgSessionLengthPerDay.add(dayData.optDouble("averagePlaytime", 0.0).toFloat())
            sessionFrequencyPerDay.add(dayData.optInt("sessionCount", 0))
            peakPlaytimePerDay.add(dayData.optDouble("peakPlaytime", 0.0).toFloat())
            dates.add(dayData.optString("date", ""))
        }

        setupTotalPlaytimeLineChart(playtimePerDay, dates)
        setupAverageSessionLengthChart(avgSessionLengthPerDay, dates)
        setupSessionFrequencyBarChart(sessionFrequencyPerDay, dates)
        setupPeakPlayTimeScatterChart(peakPlaytimePerDay, dates)
        setupGameComparisonPieChart(response.optJSONArray("playtimeComparison"))
    }

    class DateValueFormatter(private val dates: List<String>) : ValueFormatter() {
        @RequiresApi(Build.VERSION_CODES.O)
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

        @RequiresApi(Build.VERSION_CODES.O)
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return try {
                // Use the index value to get the corresponding date from the list
                val index = value.toInt() // Convert Float to Int to get the index
                if (index in dates.indices) {
                    val dateString = dates[index] // Get the date string at the index
                    val date = ZonedDateTime.parse(dateString, formatter)
                    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy") // Format as DD/MM/YY
                    date.format(outputFormatter)
                } else {
                    "Invalid Date"
                }
            } catch (e: Exception) {
                "Invalid Date"
            }
        }
    }
    // Total Playtime Line Chart with Professional Look
    private fun setupTotalPlaytimeLineChart(playtimePerDay: List<Float>, dates: List<String>) {
        val entries = playtimePerDay.mapIndexed { index, playtime -> Entry(index.toFloat(), playtime) }

        val lineDataSet = LineDataSet(entries, "Total Playtime (Hours)").apply {
            color = Color.parseColor("#FFB347") // Accent color from your palette (Bright Coral Red)
            lineWidth = 2f
            setCircleColor(Color.parseColor("#FFB347")) // Match circle color with line color
            circleRadius = 4f
            valueTextSize = 0f // Disable values on top of points for cleaner look
            setDrawValues(false) // Hide values on the chart
            mode = LineDataSet.Mode.LINEAR // Smooth line interpolation
            setDrawCircles(true) // Show circles at data points
            setDrawCircleHole(true) // Hollow circles for a modern look
            circleHoleRadius = 2f
            circleHoleColor = Color.parseColor("#FFFFFF") // White hole inside circles
            highLightColor = Color.parseColor("#FFB347") // Highlight color for selected points
            setDrawHorizontalHighlightIndicator(false) // Disable horizontal highlight lines
        }

        totalPlaytimeLineChart.apply {
            data = LineData(lineDataSet)

            xAxis.apply {
                valueFormatter = DateValueFormatter(dates)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f // Rotate labels for better readability
                textSize = 6f
                textColor = Color.parseColor("#D3D3D3") // Light gray text for secondary elements
                setDrawGridLines(false) // Disable grid lines for X-axis
                setDrawAxisLine(false) // Remove the axis line for a cleaner look
                axisMinimum = -0.5f // Add padding to the left of the first label
                axisMaximum = (dates.size - 1).toFloat() + 0.5f // Add padding to the right of the last label
            }

            axisLeft.isEnabled = false // Disable left Y-axis

            axisRight.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}h"
                    }
                }
                textSize = 10f
                textColor = Color.parseColor("#D3D3D3") // Light gray text for secondary elements
                granularity = 1f
                setDrawGridLines(false) // Disable grid lines for Y-axis
                setDrawAxisLine(false) // Remove the axis line for a cleaner look
            }

            description.isEnabled = false // Disable chart description
            legend.isEnabled = false // Disable chart legend
            setExtraOffsets(15f, 15f, 15f, 15f) // Add extra padding around the chart
            setBackgroundColor(Color.parseColor("#1E1E1E")) // Match background to app's secondaryColor
            setDrawBorders(false) // Remove chart borders for a modern look
            setNoDataText("No data available") // Custom message when no data is present
            setNoDataTextColor(Color.parseColor("#A0A0A0")) // Subtle gray for placeholder text
            invalidate() // Redraw the chart
        }
    }

    private fun setupAverageSessionLengthChart(avgSessionLengthPerDay: List<Float>, dates: List<String>) {
        val entries = avgSessionLengthPerDay.mapIndexed { index, length -> Entry(index.toFloat(), length) }

        val lineDataSet = LineDataSet(entries, "Avg Session Length (Hours)").apply {
            color = Color.parseColor("#FFB347")
            lineWidth = 2f
            setCircleColor(Color.parseColor("#FFB347"))
            circleRadius = 4f
            valueTextSize = 0f // Disable values on top of points for cleaner look
            setDrawValues(false) // Hide values on the chart
            mode = LineDataSet.Mode.LINEAR // Smooth line interpolation
            setDrawCircles(true) // Show circles at data points
            setDrawCircleHole(true) // Hollow circles for a modern look
            circleHoleRadius = 2f
            circleHoleColor = Color.parseColor("#FFFFFF") // White hole inside circles
            highLightColor = Color.parseColor("#FFB347") // Highlight color for selected points (Soft Orange)
            setDrawHorizontalHighlightIndicator(false) // Disable horizontal highlight lines
        }

        averageSessionLengthLineChart.apply {
            data = LineData(lineDataSet)

            xAxis.apply {
                valueFormatter = DateValueFormatter(dates)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f // Rotate labels for better readability
                textSize = 6f
                textColor = Color.parseColor("#D3D3D3") // Light gray text for secondary elements
                setDrawGridLines(false) // Disable grid lines for X-axis
                setDrawAxisLine(false) // Remove the axis line for a cleaner look
                axisMinimum = -0.5f // Add padding to the left of the first label
                axisMaximum = (dates.size - 1).toFloat() + 0.5f // Add padding to the right of the last label
            }

            axisLeft.isEnabled = false // Disable left Y-axis

            axisRight.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}h"
                    }
                }
                textSize = 10f
                textColor = Color.parseColor("#D3D3D3") // Light gray text for secondary elements
                granularity = 1f
                setDrawGridLines(false) // Disable grid lines for Y-axis
                setDrawAxisLine(false) // Remove the axis line for a cleaner look
            }

            description.isEnabled = false // Disable chart description
            legend.isEnabled = false // Disable chart legend
            setExtraOffsets(15f, 15f, 15f, 15f) // Add extra padding around the chart
            setBackgroundColor(Color.parseColor("#1E1E1E")) // Match background to app's secondaryColor
            setDrawBorders(false) // Remove chart borders for a modern look
            setNoDataText("No data available") // Custom message when no data is present
            setNoDataTextColor(Color.parseColor("#A0A0A0")) // Subtle gray for placeholder text
            invalidate() // Redraw the chart
        }
    }

    private fun setupSessionFrequencyBarChart(sessionFrequencyPerDay: List<Int>, dates: List<String>) {
        val entries = sessionFrequencyPerDay.mapIndexed { index, frequency -> BarEntry(index.toFloat(), frequency.toFloat()) }

        val barDataSet = BarDataSet(entries, "Session Frequency").apply {
            color = Color.parseColor("#FFB347")
            valueTextSize = 10f // Reduced text size for better readability
            setValueTextColor(Color.parseColor("#FFFFFF")) // White text for bar values
            setDrawValues(true) // Show values on top of bars
        }

        sessionFrequencyBarChart.apply {
            data = BarData(barDataSet).apply {
                barWidth = 0.4f // Adjust bar width for a cleaner look
            }

            xAxis.apply {
                valueFormatter = DateValueFormatter(dates)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f // Rotate labels for better readability
                textSize = 6f
                textColor = Color.parseColor("#D3D3D3") // Light gray text for secondary elements
                setDrawGridLines(false) // Disable grid lines for X-axis
                setDrawAxisLine(false) // Remove the axis line for a cleaner look
                axisMinimum = -0.5f // Add padding to the left of the first label
                axisMaximum = (dates.size - 1).toFloat() + 0.5f // Add padding to the right of the last label
            }

            axisLeft.isEnabled = false // Disable left Y-axis

            axisRight.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} sessions" // Display "sessions" instead of "h"
                    }
                }
                textSize = 10f
                textColor = Color.parseColor("#D3D3D3") // Light gray text for secondary elements
                granularity = 1f
                setDrawGridLines(false) // Disable grid lines for Y-axis
                setDrawAxisLine(false) // Remove the axis line for a cleaner look
            }

            description.isEnabled = false // Disable chart description
            legend.isEnabled = false // Disable chart legend
            setExtraOffsets(15f, 15f, 15f, 15f) // Add extra padding around the chart
            setBackgroundColor(Color.parseColor("#1E1E1E")) // Match background to app's secondaryColor
            setDrawBorders(false) // Remove chart borders for a modern look
            setNoDataText("No data available") // Custom message when no data is present
            setNoDataTextColor(Color.parseColor("#A0A0A0")) // Subtle gray for placeholder text
            invalidate() // Redraw the chart
        }
    }
    // Peak Play Time Scatter Chart
    private fun setupPeakPlayTimeScatterChart(peakPlaytimePerDay: List<Float>, dates: List<String>) {
        val entries = peakPlaytimePerDay.mapIndexed { index, peakPlaytime -> Entry(index.toFloat(), peakPlaytime) }

        val scatterDataSet = ScatterDataSet(entries, "Peak Play Time").apply {
            color = Color.parseColor("#FFB347")
            setScatterShape(ScatterChart.ScatterShape.CIRCLE) // Use circles for scatter points
            scatterShapeSize = 10f // Slightly larger dots for better visibility
            valueTextSize = 0f // Disable values on top of points for cleaner look
            setDrawValues(false) // Hide values on the chart
        }

        val scatterData = ScatterData(scatterDataSet)
        peakPlayTimeScatterChart.apply {
            data = scatterData

            xAxis.apply {
                valueFormatter = DateValueFormatter(dates) // Use the updated DateValueFormatter
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f // Rotate labels for better readability
                textSize = 6f
                textColor = Color.parseColor("#D3D3D3") // Light gray text for secondary elements
                setDrawGridLines(false) // Disable grid lines for X-axis
                setDrawAxisLine(false) // Remove the axis line for a cleaner look
                axisMinimum = -0.5f // Add padding to the left of the first label
                axisMaximum = (dates.size - 1).toFloat() + 0.5f // Add padding to the right of the last label
            }

            axisLeft.isEnabled = false // Disable left Y-axis

            axisRight.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}h" // Display hours
                    }
                }
                textSize = 10f
                textColor = Color.parseColor("#D3D3D3") // Light gray text for secondary elements
                granularity = 1f
                setDrawGridLines(false) // Disable grid lines for Y-axis
                setDrawAxisLine(false) // Remove the axis line for a cleaner look
            }

            description.isEnabled = false // Disable chart description
            legend.isEnabled = false // Disable chart legend
            setExtraOffsets(15f, 15f, 15f, 15f) // Add extra padding around the chart
            setBackgroundColor(Color.parseColor("#1E1E1E")) // Match background to app's secondaryColor
            setDrawBorders(false) // Remove chart borders for a modern look
            setNoDataText("No data available") // Custom message when no data is present
            setNoDataTextColor(Color.parseColor("#A0A0A0")) // Subtle gray for placeholder text
            invalidate() // Redraw the chart
        }
    }
    private fun setupGameComparisonPieChart(playtimeComparison: JSONArray?) {
        val entries = ArrayList<PieEntry>()
        playtimeComparison?.let {
            for (i in 0 until it.length()) {
                val gameData = it.getJSONObject(i)
                val gameName = gameData.optString("game", "Unknown Game")
                val playtime = gameData.optDouble("totalPlaytime", 0.0)
                if (playtime > 0) entries.add(PieEntry(playtime.toFloat(), gameName))
            }
        }

        val pieDataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#FF6B6B"), // Accent color from your palette (Bright Coral Red)
                Color.parseColor("#3399FF"), // Light blue
                Color.parseColor("#FFB347"), // Interactive color (Soft Orange)
                Color.parseColor("#33CC99")  // Muted green
            )
            valueTextSize = 10f // Reduced text size for better readability
            valueTextColor = Color.parseColor("#FFFFFF") // White text for high contrast
            setDrawValues(true) // Show values on slices
        }

        gameComparisonPieChart.apply {
            data = PieData(pieDataSet).apply {
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}%" // Display percentages as integers
                    }
                })
            }

            setUsePercentValues(true) // Display values as percentages
            description.isEnabled = false // Disable chart description
            legend.isEnabled = true // Enable legend for better understanding
            legend.textColor = Color.parseColor("#D3D3D3") // Light gray text for legend
            legend.textSize = 10f // Adjust legend text size
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL

            setEntryLabelColor(Color.parseColor("#FFFFFF")) // White text for slice labels
            setEntryLabelTextSize(10f) // Adjust slice label text size
            setExtraOffsets(15f, 15f, 15f, 15f) // Add extra padding around the chart
            setBackgroundColor(Color.parseColor("#1E1E1E")) // Match background to app's secondaryColor
            setDrawHoleEnabled(true) // Enable a hole in the center of the pie chart
            setHoleColor(Color.TRANSPARENT) // Transparent hole for a modern look
            setTransparentCircleColor(Color.parseColor("#2F2F2F")) // Slightly darker transparent circle
            setTransparentCircleAlpha(100) // Adjust transparency of the circle
            invalidate() // Redraw the chart
        }
    }
}