package com.muhaimen.arenax.gamesDashboard

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.muhaimen.arenax.R
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_game_analytics)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        gameName = findViewById(R.id.game_name)
        totalHours = findViewById(R.id.total_hours)
        gameIcon = findViewById(R.id.game_icon)
        totalPlaytimeLineChart = findViewById(R.id.screenTimeLineChart)
        averageSessionLengthLineChart = findViewById(R.id.averageSessionLengthChart)
        sessionFrequencyBarChart = findViewById(R.id.sessionFrequencyBarChart)
        peakPlayTimeScatterChart = findViewById(R.id.peakPlayTimeChart)
        gameComparisonPieChart = findViewById(R.id.gameComparisonPieChart)
        backButton = findViewById(R.id.backButton)

        auth = FirebaseAuth.getInstance()

        val intent = intent
        val game = intent.getStringExtra("GAME_NAME")
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
        val userId = auth.currentUser?.uid ?: return

        val requestBody = JSONObject().apply {
            put("gameName", game)
            put("userId", userId)
        }

        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                Log.d(TAG, "Response received: $response")
                parseAndPopulateCharts(response) // Call the function to parse and set up charts
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
                "" // Return empty string for null or empty input
            }
            url.startsWith("http://") || url.startsWith("https://") -> url // Return the URL as is
            else -> "https:$url" // Prepend with https if it starts with //
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
                    val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
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
            color = Color.parseColor("#3366CC")  // Muted blue for line
            lineWidth = 2f
            setCircleColor(Color.parseColor("#3366CC"))
            circleRadius = 4f
            valueTextSize = 12f
            setDrawValues(false)
        }

        totalPlaytimeLineChart.apply {
            data = LineData(lineDataSet)
            xAxis.apply {
                valueFormatter = DateValueFormatter(dates)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f
                textSize = 12f
                setDrawGridLines(false)
            }
            axisLeft.isEnabled = false
            axisRight.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) = "${value.toInt()}h"
                }
                textSize = 12f
                granularity = 1f
                setDrawGridLines(false)
            }
            description.isEnabled = false
            legend.isEnabled = false
            setExtraOffsets(15f, 15f, 15f, 15f)
            invalidate()
        }
    }

    // Average Session Length Line Chart
    private fun setupAverageSessionLengthChart(avgSessionLengthPerDay: List<Float>, dates: List<String>) {
        val entries = avgSessionLengthPerDay.mapIndexed { index, length -> Entry(index.toFloat(), length) }

        val lineDataSet = LineDataSet(entries, "Avg Session Length (Hours)").apply {
            color = Color.parseColor("#FF9900")  // Muted orange
            lineWidth = 2f
            setCircleColor(Color.parseColor("#FF9900"))
            circleRadius = 4f
            valueTextSize = 12f
            setDrawValues(false)
        }

        averageSessionLengthLineChart.apply {
            data = LineData(lineDataSet)
            xAxis.apply {
                valueFormatter = DateValueFormatter(dates)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f
                textSize = 12f
                setDrawGridLines(false)
            }
            axisLeft.isEnabled = false
            axisRight.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) = "${value.toInt()}h"
                }
                textSize = 12f
                granularity = 1f
                setDrawGridLines(false)
            }
            description.isEnabled = false
            legend.isEnabled = false
            setExtraOffsets(15f, 15f, 15f, 15f)
            invalidate()
        }
    }

    // Session Frequency Bar Chart
    private fun setupSessionFrequencyBarChart(sessionFrequencyPerDay: List<Int>, dates: List<String>) {
        val entries = sessionFrequencyPerDay.mapIndexed { index, frequency -> BarEntry(index.toFloat(), frequency.toFloat()) }

        val barDataSet = BarDataSet(entries, "Session Frequency").apply {
            color = Color.parseColor("#339966")  // Muted green
            valueTextSize = 12f
        }

        sessionFrequencyBarChart.apply {
            data = BarData(barDataSet)
            xAxis.apply {
                valueFormatter = DateValueFormatter(dates)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f
                textSize = 12f
                setDrawGridLines(false)
            }
            axisLeft.isEnabled = false
            axisRight.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) = "${value.toInt()}h"
                }
                textSize = 12f
                granularity = 1f
                setDrawGridLines(false)
            }
            description.isEnabled = false
            legend.isEnabled = false
            setExtraOffsets(15f, 15f, 15f, 15f)
            invalidate()
        }
    }

    // Peak Play Time Scatter Chart
    private fun setupPeakPlayTimeScatterChart(peakPlaytimePerDay: List<Float>, dates: List<String>) {
        val entries = peakPlaytimePerDay.mapIndexed { index, peakPlaytime -> Entry(index.toFloat(), peakPlaytime) }

        val scatterDataSet = ScatterDataSet(entries, "Peak Play Time").apply {
            color = Color.parseColor("#FFA500")  // A softer orange color for dots
            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            scatterShapeSize = 8f
            valueTextSize = 8f
            setDrawValues(false)
        }

        val scatterData = ScatterData(scatterDataSet)
        peakPlayTimeScatterChart.data = scatterData

        peakPlayTimeScatterChart.xAxis.apply {
            valueFormatter = DateValueFormatter(dates) // Use the updated DateValueFormatter
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            labelRotationAngle = -30f
            textSize = 10f
            setDrawGridLines(false)
            textColor = Color.DKGRAY
        }

        peakPlayTimeScatterChart.axisLeft.isEnabled = false
        peakPlayTimeScatterChart.axisRight.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = "${value.toInt()}h"
            }
            textSize = 10f
            granularity = 1f
            setDrawGridLines(false)
            textColor = Color.DKGRAY
        }

        peakPlayTimeScatterChart.description.isEnabled = false
        peakPlayTimeScatterChart.legend.isEnabled = false
        peakPlayTimeScatterChart.setExtraOffsets(10f, 10f, 10f, 10f)
        peakPlayTimeScatterChart.invalidate()
    }

    // Game Comparison Pie Chart
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
                Color.parseColor("#FF5733"), // Muted red
                Color.parseColor("#33FFBD"), // Mint green
                Color.parseColor("#FFC300"), // Soft yellow
                Color.parseColor("#3399FF")  // Light blue
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        gameComparisonPieChart.apply {
            data = PieData(pieDataSet)
            setUsePercentValues(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            setExtraOffsets(15f, 15f, 15f, 15f)
            invalidate()
        }
    }
}