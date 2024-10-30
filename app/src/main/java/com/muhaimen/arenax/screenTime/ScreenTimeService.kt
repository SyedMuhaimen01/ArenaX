package com.muhaimen.arenax.screenTime

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.R
import com.muhaimen.arenax.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class ScreenTimeService : Service() {
    private val CHANNEL_ID = "ScreenTimeServiceChannel"
    private lateinit var auth: FirebaseAuth
    private lateinit var queue: RequestQueue
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var handler: Handler
    private var usageCheckRunnable: Runnable? = null
    private val retryInterval: Long = 30 * 60 * 1000 // 30 minutes in milliseconds
    private val usageCheckInterval: Long = 60 * 1000 // 1 minute in milliseconds
    private var dataSentToday = false // Flag to track daily data send

    private val sessionData = mutableMapOf<String, MutableList<Long>>() // Store session lengths per game
    private var sessionCount = mutableMapOf<String, Int>() // Store session count per game
    private val currentSessionStartTime = mutableMapOf<String, Long>() // Track current session start time

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, getNotification())
        queue = Volley.newRequestQueue(this)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        handler = Handler()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        auth = FirebaseAuth.getInstance()
        startUsageCheck()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("NewApi")
    private fun getNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Time Tracker")
            .setContentText("Tracking screen time...")
            .setSmallIcon(R.mipmap.appicon2)
            .setPriority(Notification.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Screen Time Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startUsageCheck() {
        usageCheckRunnable = object : Runnable {
            override fun run() {
                checkAppUsage()
                handler.postDelayed(this, usageCheckInterval)
            }
        }
        handler.post(usageCheckRunnable as Runnable)
    }

    private fun checkAppUsage() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - usageCheckInterval

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        usageStatsList.forEach { usageStats ->
            val packageName = usageStats.packageName
            val totalTimeInForeground = usageStats.totalTimeInForeground

            if (totalTimeInForeground > 0) {
                val currentSessionEndTime = System.currentTimeMillis()
                if (!currentSessionStartTime.containsKey(packageName)) {
                    // Start a new session
                    currentSessionStartTime[packageName] = currentSessionEndTime
                } else {
                    // Existing session: check if it should end
                    val sessionLength = currentSessionEndTime - currentSessionStartTime[packageName]!!
                    sessionData.getOrPut(packageName) { mutableListOf() }.add(sessionLength)
                    sessionCount[packageName] = sessionCount.getOrDefault(packageName, 0) + 1

                    // Reset the session start time for the next session
                    currentSessionStartTime[packageName] = currentSessionEndTime
                }
            }
        }

        // Send metrics to backend at 8 AM if not already sent today
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        if (currentHour == 8 && currentMinute == 0 && !dataSentToday) {
            sendMetricsToBackend()
        }
    }

    private fun sendMetricsToBackend() {
        val userId = auth.currentUser?.uid ?: return
        val gameMetrics = JSONObject().apply {
            sessionData.forEach { (packageName, sessionLengths) ->
                val totalPlaytimeMillis = sessionLengths.sum()
                val averagePlaytimeMillis = sessionLengths.average()
                val peakPlaytimeMillis = sessionLengths.maxOrNull()

                put(packageName, JSONObject().apply {
                    put("totalSessions", sessionCount[packageName])
                    put("sessionLengths", sessionLengths.map { it / 3600000.0 })
                    put("totalPlaytime", totalPlaytimeMillis / 3600000.0)
                    put("averagePlaytime", averagePlaytimeMillis / 3600000.0)
                    put("peakPlaytime", peakPlaytimeMillis?.div(3600000.0))
                })
            }
        }

        val jsonObject = JSONObject().apply {
            put("user_id", userId)
            put("game_metrics", gameMetrics)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Constants.SERVER_URL}gameMetrics/user/$userId/gamesSessionMetrics",
            jsonObject,
            { response ->
                Log.d("ScreenTimeService", "Successfully sent metrics to backend: $response")
                dataSentToday = true
                sessionData.clear()
                sessionCount.clear()
                currentSessionStartTime.clear()
            },
            { error ->
                Log.e("ScreenTimeService", "Error sending metrics to backend: ${error.message}")
                if (!dataSentToday) {
                    scheduleRetry()
                }
            }
        )

        queue.add(request)
    }

    private fun scheduleRetry() {
        handler.postDelayed({ sendMetricsToBackend() }, retryInterval)
        Log.d("ScreenTimeService", "Retrying in ${retryInterval / 60000} minutes")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(usageCheckRunnable ?: return)
    }
}
