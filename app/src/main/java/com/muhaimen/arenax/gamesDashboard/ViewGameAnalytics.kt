package com.muhaimen.arenax.gamesDashboard

import android.os.Bundle
import android.view.MotionEvent
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

class ViewGameAnalytics : AppCompatActivity() {
    private lateinit var gameName: TextView
    private lateinit var totalHours: TextView
    private lateinit var gameIcon: ImageView
    private lateinit var totalPlaytimeLineChart: LineChart
    private lateinit var averageSessionLengthLineChart: LineChart
    private lateinit var sessionFrequencyBarChart: BarChart
    private lateinit var peakPlayTimeScatterChart: ScatterChart
    private lateinit var gameComparisonPieChart: PieChart

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

        // Retrieve game name from the intent
        val intent = intent
        val game = intent.getStringExtra("GAME_NAME")
        if (game != null) {
            gameName.text = game
            // Fetch the game analytics data for the specified game
            fetchUserGameStats(game)
        } else {
            // Handle case where game is null
            gameName.text = "Unknown Game"
        }
    }

    private fun fetchUserGameStats(game: String) {
        val url = "http://192.168.100.6:3000/analytics/gameAnalytics/$game" // Append game name to the URL
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                parseAndPopulateCharts(response)
            },
            { error ->
                // Handle error
                error.printStackTrace()
            })

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest)
    }

    private fun convertToUrl(url: String): String {
        return when {
            url.isNullOrEmpty() -> "" // Return empty string for null or empty input
            url.startsWith("http://") || url.startsWith("https://") -> url // Return the URL as is
            else -> "https:$url" // Prepend with https if it starts with //
        }
    }

    private fun parseAndPopulateCharts(response: JSONObject) {
        // Parse the response and extract the data needed for the charts
        val gameName = response.getString("gameName") // Extract game name
        val gameLogo = response.getString("gameLogo") // Extract game logo
        val totalPlaytimeData = response.getJSONArray("totalPlaytime") // Extract total playtime data
        val totalHoursSpent = response.getInt("totalHoursSpent") // Extract total hours spent in that game
        val sessionFrequencyData = response.getJSONArray("sessionFrequency") // Extract session frequency data
        val peakPlayTimeData = response.getJSONArray("peakPlayTime") // Extract peak play time data
        val gameComparisonData = response.getJSONObject("gameComparison") // Extract game comparison data
        totalHours.text = "Total Hours: $totalHoursSpent"

        val logoUrl = convertToUrl(gameLogo)

        Glide.with(this) // Use 'this' to refer to the Activity context
            .load(logoUrl) // Load the URL
            .placeholder(R.drawable.circle) // Placeholder image while loading
            .error(R.drawable.circle) // Error image if the load fails
            .circleCrop() // Crop to circle
            .into(gameIcon)

        // Extracting specific values from the gameComparison object
        val userTotalHours = gameComparisonData.getInt("userTotalHours") // Extract user total hours
        val otherGamesTotalHours = gameComparisonData.getInt("otherGamesTotalHours") // Extract other games total hours
        val totalUsersCompared = gameComparisonData.getInt("totalUsersCompared") // Extract total users compared

        // Convert JSON arrays to Kotlin Lists
        val totalPlaytime = (0 until totalPlaytimeData.length()).map { totalPlaytimeData[it].toString().toFloat() }
        val sessionFrequency = (0 until sessionFrequencyData.length()).map { sessionFrequencyData[it].toString().toFloat() }
        val peakPlayTimes = (0 until peakPlayTimeData.length()).map { peakPlayTimeData[it].toString().toFloat() }

        // Set the total hours spent text view
        totalHours.text = "Total Hours: $totalHoursSpent"

        // Set up the charts with the fetched data
        setupTotalPlaytimeLineChart(totalPlaytime, listOf("Playtime")) // You can adjust labels based on your data
        setupAverageSessionLengthChart(emptyList(), emptyList()) // Keep this empty for now as data isn't available
        setupSessionFrequencyBarChart(sessionFrequency, listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
        setupPeakPlayTimeScatterChart(peakPlayTimes)
        setupGameComparisonPieChart(listOf(userTotalHours.toFloat(), otherGamesTotalHours.toFloat()), listOf("User", "Others"))
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
            setDrawGridLines(false)
        }
        totalPlaytimeLineChart.axisRight.isEnabled = false
        totalPlaytimeLineChart.invalidate()
    }

    private fun setupAverageSessionLengthChart(sessionData: List<Float>, labels: List<String>) {
        // Placeholder for average session length as data is not being used currently
        // You may implement this later when the data is available
    }

    private fun setupSessionFrequencyBarChart(sessionData: List<Float>, labels: List<String>) {
        val entries = sessionData.mapIndexed { index, sessions ->
            com.github.mikephil.charting.data.BarEntry(index.toFloat(), sessions)
        }

        val barDataSet = BarDataSet(entries, "Sessions Frequency").apply {
            color = ColorTemplate.COLORFUL_COLORS[3]
        }

        val barData = BarData(barDataSet)
        sessionFrequencyBarChart.data = barData
        sessionFrequencyBarChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawLabels(true)
            setDrawGridLines(false)
        }
        sessionFrequencyBarChart.axisLeft.apply {
            setDrawGridLines(false)
        }
        sessionFrequencyBarChart.axisRight.isEnabled = false
        sessionFrequencyBarChart.invalidate()
    }

    private fun setupPeakPlayTimeScatterChart(data: List<Float>) {
        val scatterEntries = data.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val scatterDataSet = ScatterDataSet(scatterEntries, "Peak Play Times").apply {
            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            color = ColorTemplate.COLORFUL_COLORS[4]
            scatterShapeSize = 10f
        }

        val scatterData = ScatterData(scatterDataSet)
        peakPlayTimeScatterChart.data = scatterData
        peakPlayTimeScatterChart.invalidate()
    }

    private fun setupGameComparisonPieChart(data: List<Float>, labels: List<String>) {
        val pieDataSet = PieDataSet(data.mapIndexed { index, value -> PieEntry(value, labels[index]) }, "Game Comparison").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
        }

        val pieData = PieData(pieDataSet)
        gameComparisonPieChart.data = pieData
        gameComparisonPieChart.invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // Dismiss the keyboard or any touch actions
        }
        return super.onTouchEvent(event)
    }
}
