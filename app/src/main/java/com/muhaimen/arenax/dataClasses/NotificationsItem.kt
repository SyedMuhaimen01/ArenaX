package com.muhaimen.arenax.dataClasses

data class NotificationsItem(
    val profilePicture: String = "", // URL for the profile picture
    val username: String = "", // Username of the sender
    val message: String = "", // Notification message or description
    val receiverId: String = "", // ID of the user receiving the notification
    val notificationId: String = "", // Unique ID for the notification (optional for tracking)
    val timestamp: Long = System.currentTimeMillis() // Timestamp of the notification
)
