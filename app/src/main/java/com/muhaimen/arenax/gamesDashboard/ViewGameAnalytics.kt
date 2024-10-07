package com.muhaimen.arenax.gamesDashboard

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.muhaimen.arenax.R

class ViewGameAnalytics : AppCompatActivity() {
    private lateinit var gameName: TextView
    private lateinit var totalHours: TextView
    private lateinit var gameIcon: ImageView
    private lateinit var totalPlaytimeLineChart: LineChart
    private lateinit var averageSessionLengthLineChart: LineChart
    private lateinit var sessionFrequencyBarChart: BarChart
    private lateinit var peakPlayTimeScatterChart: ScatterChart
    private lateinit var gameComparisonPieChart: PieChart
    // Dummy data for peak play time heatmap
    val peakPlayTimeData = arrayOf(
        // Monday
        floatArrayOf(14f, 15f, 16f, 17f), // 2 PM, 3 PM, 4 PM, 5 PM
        floatArrayOf(0f, 0f, 0f, 0f),    // Monday (Y-values - Frequency)

        // Tuesday
        floatArrayOf(14f, 20f),           // 2 PM, 8 PM
        floatArrayOf(1f, 0f),             // Tuesday (Y-values - Frequency)

        // Wednesday
        floatArrayOf(12f, 21f),           // 12 PM, 9 PM
        floatArrayOf(2f, 0f),             // Wednesday (Y-values - Frequency)

        // Thursday
        floatArrayOf(14f, 15f, 16f, 19f, 20f), // 2 PM, 3 PM, 4 PM, 7 PM, 8 PM
        floatArrayOf(3f, 3f, 2f, 1f, 2f),      // Thursday (Y-values - Frequency)

        // Friday
        floatArrayOf(13f, 22f),           // 1 PM, 10 PM
        floatArrayOf(0f, 1f),             // Friday (Y-values - Frequency)

        // Saturday
        floatArrayOf(11f, 18f),           // 11 AM, 6 PM
        floatArrayOf(0f, 1f),             // Saturday (Y-values - Frequency)

        // Sunday
        floatArrayOf(16f, 21f),           // 4 PM, 9 PM
        floatArrayOf(0f, 2f)              // Sunday (Y-values - Frequency)
    )


    val peakPlayTimeLabelsX = arrayOf(
        "12 AM", "1 AM", "2 AM", "3 AM", "4 AM", "5 AM", "6 AM", "7 AM", "8 AM", "9 AM",
        "10 AM", "11 AM", "12 PM", "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM",
        "7 PM", "8 PM", "9 PM", "10 PM", "11 PM"
    )
    private val peakPlayTimeLabelsY = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")


    // Dummy data for session frequency
    private val sessionFrequencyData = listOf(5f, 8f, 12f, 15f, 3f, 6f, 11f) // Number of sessions
    private val sessionFrequencyLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Dummy data for demonstration
    private val monthlyPlaytimeData = listOf(300f, 450f, 500f, 600f, 800f) // Total playtime for months
    private val monthlyLabels = listOf("Jan", "Feb", "Mar", "Apr", "May")

    val dummyScreenTimeData = listOf(120f, 200f, 150f, 80f) // Screen time in hours
    val dummyGameNames = listOf("Game A", "Game B", "Game C", "Game D") // Game names


    private val weeklyPlaytimeDataMap = mapOf(
        "July" to listOf(70f, 90f, 100f, 80f), // Weekly data for July
        "August" to listOf(50f, 60f, 75f, 65f) // Weekly data for August
    )
    private val weeklyLabels = listOf("Week 1", "Week 2", "Week 3", "Week 4")

    private val dailyPlaytimeDataMap = mapOf(
        "July" to listOf(2f, 3f, 4f, 5f, 6f, 2f, 1f), // Daily data for July
        "Week 2" to listOf(3f, 5f, 4f, 6f, 2f, 7f, 5f)
    )
    private val dailyLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    private var currentDataSet: String = "Monthly" // To track current dataset type
    private var currentMonth: String = "July" // Assume July is the selected month

    // Dummy data for Average Session Length
    private val averageSessionLengths = listOf(1.5f, 2.0f, 1.75f, 2.5f, 3.0f) // Average session lengths in hours
    private val sessionNumbers = listOf(0f, 5f, 10f, 15f, 20f) // Grouping sessions in intervals of 5


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_game_analytics)

        // Handle window insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gameName = findViewById(R.id.game_name)
        totalHours = findViewById(R.id.total_hours)
        gameIcon = findViewById(R.id.game_icon)
        totalPlaytimeLineChart = findViewById(R.id.screenTimeLineChart)
        averageSessionLengthLineChart = findViewById(R.id.averageSessionLengthChart)
        sessionFrequencyBarChart = findViewById(R.id.sessionFrequencyBarChart)
        peakPlayTimeScatterChart = findViewById(R.id.peakPlayTimeChart)
        gameComparisonPieChart = findViewById(R.id.gameComparisonPieChart)
        gameIcon.setImageResource(R.drawable.game_icon_foreground)
        gameName.text = "Game Name"
        totalHours.text = "Total Hours: 300"

        // Set up the charts with data
        setupTotalPlaytimeLineChart(monthlyPlaytimeData, monthlyLabels)
        setupAverageSessionLengthChart(averageSessionLengths, sessionNumbers.map { it.toString() }) // Convert to String
        setupSessionFrequencyBarChart(sessionFrequencyData, sessionFrequencyLabels)
        setupPeakPlayTimeScatterChart(peakPlayTimeData)
        // Set up the pie chart
        setupGameComparisonPieChart(dummyScreenTimeData, dummyGameNames)
        // Enable zooming and panning for line charts
        enableZoom()
    }

    private fun setupTotalPlaytimeLineChart(playtimeData: List<Float>, labels: List<String>) {
        val entries = playtimeData.mapIndexed { index, time ->
            Entry(index.toFloat(), time)
        }

        val lineDataSet = LineDataSet(entries, "Total Playtime (hours)").apply {
            color = ColorTemplate.COLORFUL_COLORS[0]
            lineWidth = 2f
            setCircleColor(ColorTemplate.COLORFUL_COLORS[1])
            circleRadius = 4f
        }

        val lineData = LineData(lineDataSet)
        totalPlaytimeLineChart.data = lineData
        totalPlaytimeLineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawLabels(true)
            setDrawGridLines(false) // Disable vertical grid lines
        }
        totalPlaytimeLineChart.axisLeft.apply {
            setDrawGridLines(false) // Disable horizontal grid lines
        }
        totalPlaytimeLineChart.axisRight.isEnabled = false
        totalPlaytimeLineChart.invalidate()
    }

    private fun setupAverageSessionLengthChart(sessionData: List<Float>, labels: List<String>) {
        val entries = sessionData.mapIndexed { index, time ->
            Entry(index.toFloat(), time)
        }

        val lineDataSet = LineDataSet(entries, "Average Session Length (hours)").apply {
            color = ColorTemplate.COLORFUL_COLORS[2] // Choose a different color
            lineWidth = 2f
            setCircleColor(ColorTemplate.COLORFUL_COLORS[3])
            circleRadius = 4f
        }

        val lineData = LineData(lineDataSet)
        averageSessionLengthLineChart.data = lineData
        averageSessionLengthLineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawLabels(true)
            setDrawGridLines(false) // Disable vertical grid lines
        }
        averageSessionLengthLineChart.axisLeft.apply {
            setDrawGridLines(false) // Disable horizontal grid lines
        }
        averageSessionLengthLineChart.axisRight.isEnabled = false
        averageSessionLengthLineChart.description.isEnabled = false // Disable the description
        averageSessionLengthLineChart.invalidate()
    }

    private fun setupSessionFrequencyBarChart(sessionData: List<Float>, labels: List<String>) {
        val entries = sessionData.mapIndexed { index, sessions ->
            com.github.mikephil.charting.data.BarEntry(index.toFloat(), sessions)
        }

        val barDataSet = BarDataSet(entries, "Sessions Frequency").apply {
            color = ColorTemplate.COLORFUL_COLORS[3] // Choose a color for bars
        }

        val barData = BarData(barDataSet)
        sessionFrequencyBarChart.data = barData
        sessionFrequencyBarChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawLabels(true)
            setDrawGridLines(false) // Disable vertical grid lines
        }
        sessionFrequencyBarChart.axisLeft.apply {
            setDrawGridLines(false) // Disable horizontal grid lines
        }
        sessionFrequencyBarChart.axisRight.isEnabled = false
        sessionFrequencyBarChart.description.isEnabled = false // Disable the description
        sessionFrequencyBarChart.invalidate()
    }

    private fun setupPeakPlayTimeScatterChart(data: Array<FloatArray>) {
        val scatterEntries = mutableListOf<Entry>()

        for (day in 0 until 7) { // For each day of the week
            val xValues = peakPlayTimeData[day * 2] // X-values for the day
            val yValues = peakPlayTimeData[day * 2 + 1] // Y-values for the day

            for (i in xValues.indices) {
                scatterEntries.add(Entry(xValues[i], day.toFloat())) // Create scatter point
            }
        }

        // Scatter data set
        val scatterDataSet = ScatterDataSet(scatterEntries, "Peak Play Time").apply {
            color = ColorTemplate.COLORFUL_COLORS[4]
            scatterShapeSize = 8f
        }

        // Create scatter data and set it to the chart
        val scatterData = ScatterData(scatterDataSet)
        peakPlayTimeScatterChart.data = scatterData

        // Configure the X-axis for 24-hour format
        peakPlayTimeScatterChart.xAxis.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    val hour = value.toInt()
                    return String.format("%02d:00", hour) // Format to "00:00"
                }
            }
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawLabels(true)
            setDrawGridLines(false)
            axisMinimum = 0f // Minimum x-value
            axisMaximum = 24f // Maximum x-value
        }

        // Configure Y-axis for days of the week
        peakPlayTimeScatterChart.axisLeft.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    return when (value.toInt()) {
                        0 -> "Monday"
                        1 -> "Tuesday"
                        2 -> "Wednesday"
                        3 -> "Thursday"
                        4 -> "Friday"
                        5 -> "Saturday"
                        6 -> "Sunday"
                        else -> ""
                    }
                }
            }
            setDrawGridLines(false)
        }

        peakPlayTimeScatterChart.axisRight.isEnabled = false
        peakPlayTimeScatterChart.description.isEnabled = false
        peakPlayTimeScatterChart.isHorizontalScrollBarEnabled = true // Enable horizontal scrolling
        peakPlayTimeScatterChart.setScaleEnabled(true) // Enable scaling
        peakPlayTimeScatterChart.setDragEnabled(true) // Enable dragging
        peakPlayTimeScatterChart.invalidate() // Refresh the chart
    }

    private fun setupGameComparisonPieChart(screenTimeData: List<Float>, gameNames: List<String>) {
        // Prepare PieEntry list for the PieChart
        val pieEntries = screenTimeData.mapIndexed { index, time ->
            PieEntry(time, gameNames[index])
        }

        // Create a PieDataSet with the entries
        val pieDataSet = PieDataSet(pieEntries, "Screen Time by Game").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList() // Use material colors
            valueTextColor = Color.BLACK
            valueTextSize = 5f
            setDrawValues(false)

        }

        // Create PieData and set it to the chart
        val pieData = PieData(pieDataSet)
        gameComparisonPieChart.data = pieData

        // Additional chart configuration
        gameComparisonPieChart.description.isEnabled = false // Disable description
        gameComparisonPieChart.setDrawEntryLabels(false) // Set label color
        gameComparisonPieChart.animateY(1000) // Add animation
        gameComparisonPieChart.invalidate() // Refresh the chart
    }

    private fun enableZoom() {
        totalPlaytimeLineChart.isDoubleTapToZoomEnabled = true
        averageSessionLengthLineChart.isDoubleTapToZoomEnabled = true
        sessionFrequencyBarChart.isDoubleTapToZoomEnabled = true

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Preventing touch events from being handled by child views
        if (event.action == MotionEvent.ACTION_MOVE) {
            return true
        }
        return super.onTouchEvent(event)
    }
}
