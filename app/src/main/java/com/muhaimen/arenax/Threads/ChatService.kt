package com.muhaimen.arenax.Threads

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem

class ChatService : Service() {

    private lateinit var database: DatabaseReference
    private lateinit var currentUserId: String
    private lateinit var auth:FirebaseAuth

    override fun onCreate() {
        super.onCreate()

        auth=FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: return

        // Initialize Firebase Database reference for the current user
        database = FirebaseDatabase.getInstance().getReference("userData").child(currentUserId).child("chats")

        //listener for changes in the "chats" node (detects new messages)
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Log the snapshot to see if the listener is triggered
                Log.d("ChatService", "onChildAdded triggered: ${snapshot.key}")

                for (chatSnapshot in snapshot.children) {
                    val receiverSenderKey = chatSnapshot.key ?: continue
                    val receiverId = receiverSenderKey.split("-")[0]
                    val senderId = receiverSenderKey.split("-")[1]

                    Log.d("ChatService", "receiverId: $receiverId, senderId: $senderId")

                    // If the current user is the receiver
                    if (receiverId == currentUserId) {
                        val chatItem = chatSnapshot.getValue(ChatItem::class.java)
                        Log.d("ChatService", "ChatItem: $chatItem")

                        if (chatItem != null && chatItem.senderId == senderId) {
                            showNotification(chatItem)
                        }
                    }
                }
            }


        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle updates to existing messages (optional)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle message removal (optional)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle message move (optional)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the service in the foreground
        createNotificationChannel()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        // For Android O and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "chat_notifications",
                "Chat Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }



    private fun showNotification(chatItem: ChatItem) {
        // Create a notification based on the received message
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "chat_notifications"

        // Format the timestamp to a readable string
        val formattedTime = formatTimestamp(chatItem.time)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("New Message from ${chatItem.senderId}")
            .setContentText("Message: ${chatItem.message}")
            .setSubText("Sent at: $formattedTime")
            .setSmallIcon(R.mipmap.appicon2) // Replace with your app's icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(0, notification)
    }

    private fun formatTimestamp(timestamp: Long): String {
        // Convert timestamp to a human-readable format
        val dateFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        val date = java.util.Date(timestamp)
        return dateFormat.format(date)
    }
}
