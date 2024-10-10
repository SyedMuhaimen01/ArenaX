package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.utils.Constants
import java.net.URLEncoder

class ViewGameAnalytics : AppCompatActivity() {
    private lateinit var gameName: TextView
    private lateinit var totalHours: TextView
    private lateinit var gameIcon: ImageView
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
            setupGameComparisonPieChart(listOf(userTotalHours.toFloat(), otherGamesTotalHours.toFloat()), listOf("User", "Others"))
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
        totalPlaytimeLineChart.invalidate()
        Log.d(TAG, "Total Playtime Line Chart set up with data: $playtimeData")
    }

    private fun setupAverageSessionLengthChart(sessionData: List<Float>, labels: List<String>) {
        // Placeholder function for now
        Log.d(TAG, "Average Session Length Chart setup called, but data is not available.")
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

        Log.d(TAG, "Session Frequency Bar Chart set up with data: ")
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
        peakPlayTimeScatterChart.invalidate()
        Log.d(TAG, "Peak Play Time Scatter Chart set up with data: $peakPlayTimes")
    }

    private fun setupGameComparisonPieChart(comparisonData: List<Float>, labels: List<String>) {
        val entries = comparisonData.mapIndexed { index, hours ->
            PieEntry(hours, labels[index])
        }

        val pieDataSet = PieDataSet(entries, "Game Comparison").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = ColorTemplate.COLORFUL_COLORS[3]
            valueTextSize = 16f
        }

        val pieData = PieData(pieDataSet)
        gameComparisonPieChart.data = pieData
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
