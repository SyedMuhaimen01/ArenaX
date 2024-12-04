package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date

data class Story(
    val id: Int,
    val mediaUrl: String,
    val duration: Int,
    val trimmedAudioUrl: String?,
    val draggableTexts: JSONArray?, // Kept as JSONArray
    val uploadedAt: Date?, // Keep as Date to match the TIMESTAMP type
    val userName: String, // New field for user's name
    val userProfilePicture: String // New field for user's profile picture URL
) : Parcelable {

    val timeAgo: String
        get() {
            if (uploadedAt == null) return "Unknown time"
            val hoursAgo = (System.currentTimeMillis() - uploadedAt.time) / 1000 / 60 / 60
            return when {
                hoursAgo < 24 -> "$hoursAgo hours ago"
                hoursAgo < 24 * 7 -> "${hoursAgo / 24} days ago"
                hoursAgo < 24 * 365 -> "${hoursAgo / (24 * 7)} weeks ago"
                else -> SimpleDateFormat("dd/MM/yyyy").format(uploadedAt)
            }
        }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()?.let { JSONArray(it) }, // Read JSONArray from String
        if (parcel.readLong() == -1L) null else Date(parcel.readLong()), // Handle null Date
        parcel.readString() ?: "", // Read userName
        parcel.readString() ?: ""  // Read userProfilePicture
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(mediaUrl)
        parcel.writeInt(duration)
        parcel.writeString(trimmedAudioUrl)
        parcel.writeString(draggableTexts?.toString()) // Convert JSONArray to String
        parcel.writeLong(uploadedAt?.time ?: -1) // Write the timestamp; -1 indicates null
        parcel.writeString(userName) // Write userName
        parcel.writeString(userProfilePicture) // Write userProfilePicture
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Story> {
        override fun createFromParcel(parcel: Parcel): Story = Story(parcel)
        override fun newArray(size: Int): Array<Story?> = arrayOfNulls(size)
    }
}
