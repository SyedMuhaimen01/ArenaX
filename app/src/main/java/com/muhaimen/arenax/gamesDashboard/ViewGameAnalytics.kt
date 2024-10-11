package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.muhaimen.arenax.R
import org.json.JSONArray
import org.json.JSONObject
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.utils.Constants
import java.net.URLEncoder

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
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)
        // Initialize the views
        gameName = findViewById(R.id.game_name)
        totalHours = findViewById(R.id.total_hours)
        gameIcon = findViewById(R.id.game_icon)
        totalPlaytimeLineChart = findViewById(R.id.screenTimeLineChart)
        averageSessionLengthLineChart = findViewById(R.id.averageSessionLengthChart)
        sessionFrequencyBarChart = findViewById(R.id.sessionFrequencyBarChart)
        peakPlayTimeScatterChart = findViewById(R.id.peakPlayTimeChart)
        gameComparisonPieChart = findViewById(R.id.gameComparisonPieChart)
        gameIcon.setImageResource(R.drawable.game_icon_foreground)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }
        auth = FirebaseAuth.getInstance()

        // Retrieve game name from the intent
        val intent = intent
        val game = intent.getStringExtra("GAME_NAME")
        if (game != null) {
            gameName.text = game
            Log.d(TAG, "Fetching analytics for game: $game")
            // Fetch the game analytics data for the specified game
            fetchUserGameStats(game)
        } else {
            // Handle case where game is null
            gameName.text = "Unknown Game"
            Log.e(TAG, "Game name is null!")
        }
    }

    private fun fetchUserGameStats(game: String) {

        val url = "${Constants.SERVER_URL}analytics/gameAnalytics"
        Log.d(TAG, "Fetching data from URL: $url")

        val userId=auth.currentUser?.uid.toString()
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
            url.isNullOrEmpty() -> {
                Log.e(TAG, "URL is null or empty!")
                "" // Return empty string for null or empty input
            }
            url.startsWith("http://") || url.startsWith("https://") -> url // Return the URL as is
            else -> "https:$url" // Prepend with https if it starts with //
        }
    }

    @SuppressLint("SetTextI18n")
    private fun parseAndPopulateCharts(response: JSONObject) {
        try {
            val gameName = response.getString("gameName")
            val gameLogo = response.getString("gameLogo")
            val totalPlaytimeData = response.getJSONArray("totalPlaytime")
            val totalHoursSpent = response.getInt("totalHoursSpent")
            val sessionFrequencyData = response.getJSONArray("sessionFrequency")
            val peakPlayTimeData = response.getJSONArray("peakPlayTime")
            val gameComparisonData = response.getJSONObject("gameComparison")

            Log.d(TAG, "Game Name: $gameName, Total Hours Spent: $totalHoursSpent")

            totalHours.text = "Total Hours: $totalHoursSpent"

            val logoUrl = convertToUrl(gameLogo)

            Glide.with(this)
                .load(logoUrl)
                .placeholder(R.drawable.circle)
                .error(R.drawable.circle)
                .circleCrop()
                .into(gameIcon)

            val userTotalHours = gameComparisonData.getInt("userTotalHours")
            val otherGamesTotalHours = gameComparisonData.getInt("otherGamesTotalHours")
            val totalUsersCompared = gameComparisonData.getInt("totalUsersCompared")

            Log.d(TAG, "User Total Hours: $userTotalHours, Other Games Total Hours: $otherGamesTotalHours, Total Users Compared: $totalUsersCompared")

            val totalPlaytime = (0 until totalPlaytimeData.length()).map { totalPlaytimeData[it].toString().toFloat() }
            val sessionFrequency = (0 until sessionFrequencyData.length()).map { sessionFrequencyData[it].toString().toFloat() }
            val peakPlayTimes = (0 until peakPlayTimeData.length()).map { peakPlayTimeData[it].toString().toFloat() }

            totalHours.text = "Total Hours: $totalHoursSpent"

            setupTotalPlaytimeLineChart(totalPlaytime, listOf("Playtime"))
            setupAverageSessionLengthChart(emptyList(), emptyList())
            setupSessionFrequencyBarChart(sessionFrequency, listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
            setupPeakPlayTimeScatterChart(peakPlayTimes)
            setupGameComparisonPieChart(listOf(userTotalHours.toFloat(), otherGamesTotalHours.toFloat()), listOf(gameName, "Other Games"))
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response: ${e.message}")
            e.printStackTrace()
        }
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
            setDrawGridLines(false)
        }
        totalPlaytimeLineChart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
        }
        totalPlaytimeLineChart.description.isEnabled = false
        totalPlaytimeLineChart.invalidate()
        Log.d(TAG, "Total Playtime Line Chart set up with data: $playtimeData")
    }

    private fun setupAverageSessionLengthChart(sessionData: List<Float>, labels: List<String>) {
        // Create a basic dataset with 0 values for now (to ensure the chart displays)
        val entries = labels.mapIndexed { index, _ ->
            com.github.mikephil.charting.data.BarEntry(index.toFloat(), 0f)  // No data, y-value set to 0
        }

        // Create a BarDataSet with placeholder data
        val lineDataSet = LineDataSet(entries, "Average Session Length (hrs)").apply {
            color = ColorTemplate.COLORFUL_COLORS[3]  // Set bar color
            valueTextColor = Color.BLACK              // Set value text color
            valueTextSize = 12f                       // Set text size for values
        }

        // Create the BarData object, ensuring it's not empty
        val lineData = LineData(lineDataSet)

        // Set up the X-axis labels and properties
        averageSessionLengthLineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)  // Label formatter
            position = XAxis.XAxisPosition.BOTTOM             // Labels on bottom
            granularity = 1f                                  // Show every label
            setDrawLabels(true)                               // Ensure labels are drawn
            setDrawGridLines(false)                           // Remove grid lines
            labelCount = labels.size                          // Set label count
        }

        // Set up the Y-axis to display hours
        averageSessionLengthLineChart.axisLeft.apply {
            axisMinimum = 0f                  // Y-axis minimum value (hours)
            axisMaximum = 24f                 // Y-axis maximum, assuming 24 hours
            setDrawGridLines(true)            // Keep grid lines on the Y-axis
            granularity = 2f                  // Show ticks every 2 hours
            labelCount = 12                   // Show 12 ticks (every 2 hours)
        }

        // Disable the right Y-axis
        averageSessionLengthLineChart.axisRight.isEnabled = false

        // Disable chart description
        averageSessionLengthLineChart.description.isEnabled = false

        // Set the data and refresh the chart
        averageSessionLengthLineChart.data = lineData
        averageSessionLengthLineChart.setNoDataText("")  // Remove "No chart data available" message
        averageSessionLengthLineChart.invalidate()       // Refresh the chart

        Log.d(TAG, "Average Session Length Chart set up with placeholder data.")
    }


    private fun setupSessionFrequencyBarChart(sessionData: List<Float>, labels: List<String>) {
        // Ensure sessionData has entries for all 7 days (with 0 if data is missing)
        val completeSessionData = mutableListOf<Float>()
        for (i in labels.indices) {
            if (i < sessionData.size) {
                completeSessionData.add(sessionData[i])
            } else {
                completeSessionData.add(0f)  // Add 0 if no data exists for that day
            }
        }

        // Create BarEntries for each day, even if the value is 0
        val entries = completeSessionData.mapIndexed { index, sessions ->
            com.github.mikephil.charting.data.BarEntry(index.toFloat(), sessions)
        }

        // Set up the BarDataSet
        val barDataSet = BarDataSet(entries, "Sessions Frequency").apply {
            color = ColorTemplate.COLORFUL_COLORS[3]
        }

        // Prepare the BarData
        val barData = BarData(barDataSet)

        // Configure the xAxis to show all 7 labels
        sessionFrequencyBarChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)  // Use labels for x-axis
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f  // Ensure that every label is displayed
            setDrawLabels(true)
            setDrawGridLines(false)
            labelCount = labels.size  // Set the label count to the number of days
            axisMinimum = 0f
            axisMaximum = labels.size.toFloat() - 1  // Ensure x-axis spans all labels
        }

        // Configure the left axis (Y-axis)
        sessionFrequencyBarChart.axisLeft.apply {
            setDrawGridLines(false)
        }

        // Disable the right Y-axis
        sessionFrequencyBarChart.axisRight.isEnabled = false

        // Disable chart description
        sessionFrequencyBarChart.description.isEnabled = false

        // Set the bar data and refresh the chart
        sessionFrequencyBarChart.data = barData
        sessionFrequencyBarChart.invalidate()

        Log.d(TAG, "Session Frequency Bar Chart set up with complete data: $completeSessionData")
    }




    private fun setupPeakPlayTimeScatterChart(peakPlayTimes: List<Float>) {
        val entries = peakPlayTimes.mapIndexed { index, peak ->
            Entry(index.toFloat(), peak)
        }

        val scatterDataSet = ScatterDataSet(entries, "Peak Play Time").apply {
            color = ColorTemplate.COLORFUL_COLORS[2]
            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        }

        val scatterData = ScatterData(scatterDataSet)
        peakPlayTimeScatterChart.data = scatterData
        peakPlayTimeScatterChart.description.isEnabled = false
        peakPlayTimeScatterChart.invalidate()
        Log.d(TAG, "Peak Play Time Scatter Chart set up with data: $peakPlayTimes")
    }

    private fun setupGameComparisonPieChart(comparisonData: List<Float>, labels: List<String>) {
        val entries = comparisonData.mapIndexed { index, hours ->
            PieEntry(hours, labels[index])
        }

        val pieDataSet = PieDataSet(entries, "Game Comparison").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            // Hide the labels (values) on top of the pie chart
            setDrawValues(false)  // This will hide the values on top of each slice
        }

        val pieData = PieData(pieDataSet)
        gameComparisonPieChart.data = pieData
        gameComparisonPieChart.description.isEnabled = false
        gameComparisonPieChart.invalidate()
        Log.d(TAG, "Game Comparison Pie Chart set up with data: $comparisonData")
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "Touch event detected")
            }
        }
        return super.onTouchEvent(event)
    }
}
