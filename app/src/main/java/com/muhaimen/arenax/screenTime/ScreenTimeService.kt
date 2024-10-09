package com.muhaimen.arenax.screenTime

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStats
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
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.GameStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ScreenTimeService : Service() {
    private val CHANNEL_ID = "ScreenTimeServiceChannel"
    private lateinit var auth: FirebaseAuth
    private var userGames: List<GameStats> = emptyList()
    private lateinit var queue: RequestQueue
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var handler: Handler
    private var usageCheckRunnable: Runnable? = null
    private val accumulatedPlaytime = mutableMapOf<String, Long>() // Initialize as a mutable map
    private val hourlyPlaytime = mutableMapOf<String, Long>() // New variable for hourly tracking

    // Set usage check interval to 1 minute (60000 milliseconds)
    private val usageCheckInterval: Long = 1000 // Check every minute
    private val playtimeThreshold: Long = 1000 // Threshold to send playtime to backend (1 hour in milliseconds)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, getNotification())
        queue = Volley.newRequestQueue(this) // Initialize the RequestQueue
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager // Initialize UsageStatsManager
        handler = Handler() // Initialize Handler
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.IO).launch {
            auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid ?: ""
            fetchUserGames(userId)
        }
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
            .setPriority(Notification.PRIORITY_HIGH) // Set priority for visibility on newer devices
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

    private fun fetchUserGames(userId: String) {
        val url = "http://192.168.100.6:3000/usergames/user/$userId/mygames"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                val gamesArray = response.optJSONArray("games") ?: JSONArray()

                // Populate userGames with GameStats objects
                userGames = List(gamesArray.length()) { index ->
                    val gameObject = gamesArray.getJSONObject(index)
                    GameStats(
                        userGameId = gameObject.getInt("gameId"),
                        gameName = gameObject.getString("gameName"),
                        packageName = gameObject.getString("packageName"),
                        logoUrl = gameObject.getString("gameIcon"),
                        totalHours = gameObject.getInt("totalHours"),
                        avgPlaytime = 0, // Default value
                        peakPlaytime = 0 // Default value
                    )
                }

                Log.d("GameStatsRepository", "Fetched user games: $userGames")

                // Call to track screen time
                if (userGames.isNotEmpty()) {
                    trackScreenTime(userGames)
                } else {
                    Log.d("ScreenTimeService", "No games found for user: $userId")
                }
            },
            Response.ErrorListener { error ->
                Log.e("GameStatsRepository", "Error fetching user games: ${error.message}")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Content-Type" to "application/json")
            }
        }

        queue.add(jsonObjectRequest)
    }

    private fun trackScreenTime(matchedGames: List<GameStats>) {
        // Start the periodic usage check
        startUsageCheck(matchedGames)
    }

    private fun startUsageCheck(matchedGames: List<GameStats>) {
        usageCheckRunnable = object : Runnable {
            override fun run() {
                matchedGames.forEach { game ->
                    checkAppUsage(game.packageName)
                }
                handler.postDelayed(this, usageCheckInterval) // Schedule the next check
            }
        }
        handler.post(usageCheckRunnable as Runnable) // Start the runnable
    }

    private fun checkAppUsage(packageName: String?) {
        // Ensure packageName is not null before proceeding
        if (packageName == null) {
            Log.d("GameStatsRepository", "Package name is null, skipping...")
            return // Exit the method if packageName is null
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - usageCheckInterval

        Log.d("GameStatsRepository", "Checking usage for $packageName from $startTime to $endTime")

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        var totalTimeInForeground = 0L
        var isCurrentlyRunning = false

        usageStatsList.forEach { usageStats ->
            if (usageStats.packageName == packageName) {
                totalTimeInForeground += usageStats.totalTimeInForeground // Accumulate total time
                isCurrentlyRunning = true
                Log.d("GameStatsRepository", "Package: $packageName, Time in foreground: $totalTimeInForeground ms")
            }
        }

        if (isCurrentlyRunning) {
            Log.d("GameStatsRepository", "App is currently running: $packageName")

            // Get the previous accumulated playtime
            val previousAccumulated = accumulatedPlaytime[packageName] ?: 0L
            val hourlyAccumulated = hourlyPlaytime[packageName] ?: 0L

            // Calculate the change in playtime
            val changeInPlaytime = (totalTimeInForeground - previousAccumulated).coerceAtLeast(0) // Ensure no negative values

            Log.d("GameStatsRepository", "Change in playtime for $packageName: $changeInPlaytime ms")

            // Update the accumulated playtime and hourly playtime
            accumulatedPlaytime[packageName] = totalTimeInForeground
            hourlyPlaytime[packageName] = hourlyAccumulated + changeInPlaytime

            // Send the change in playtime to the backend only if it exceeds the threshold
            if (changeInPlaytime >= playtimeThreshold) {
                sendPlaytimeToBackend(applicationContext, changeInPlaytime, packageName) // Use changeInPlaytime instead of playtime
            } else {
                Log.d("GameStatsRepository", "Playtime change for $packageName is below threshold, not sending to backend")
            }

        } else {
            Log.d("GameStatsRepository", "App is not currently running: $packageName")
        }
    }

    private fun resetHourlyPlaytime() {
        // Reset the hourly playtime map at the end of the day
        hourlyPlaytime.clear()
    }

    private fun sendPlaytimeToBackend(context: Context, playtime: Long, packageName: String) {
        if (playtime > 0) {
            val userId = auth.currentUser?.uid ?: return // Get userId here
            val playtimeInMillis = playtime // Already in milliseconds

            val jsonObject = JSONObject().apply {
                put("game_package", packageName)
                put("playtime", playtimeInMillis) // Sending updated playtime
                put("user_id", userId)
            }

            val request = JsonObjectRequest(
                Request.Method.POST,
                "http://192.168.100.6:3000/analytics/user/$userId/userGameTracking",
                jsonObject,
                { response ->
                    Log.d("GameStatsRepository", "Successfully sent playtime to backend: $response")
                },
                { error ->
                    Log.e("GameStatsRepository", "Error sending playtime to backend: ${error.message}")
                    error.printStackTrace() // Print stack trace for debugging
                }
            )

            // Add the request to the queue using the provided context
            Volley.newRequestQueue(context).add(request)
        } else {
            Log.d("GameStatsRepository", "No playtime to send for $packageName")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the periodic usage check
        handler.removeCallbacks(usageCheckRunnable ?: return)
    }
}
