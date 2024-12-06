package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date

data class Story(
    val id: String,
    val mediaUrl: String,
    val duration: Int,
    val trimmedAudioUrl: String?,
    val draggableTexts: JSONArray?,
    val uploadedAt: Date?,
    val userName: String,
    val userProfilePicture: String,
    val city: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?
) : Parcelable {

    val timeAgo: String
        get() {
            if (uploadedAt == null) return "Unknown time"
            val minutesAgo = (System.currentTimeMillis() - uploadedAt.time) / 1000 / 60
            val hoursAgo = minutesAgo / 60
            return when {
                minutesAgo < 60 -> "$minutesAgo minutes ago"
                hoursAgo < 24 -> "$hoursAgo hours ago"
                hoursAgo < 24 * 7 -> "${hoursAgo / 24} days ago"
                hoursAgo < 24 * 365 -> "${hoursAgo / (24 * 7)} weeks ago"
                else -> SimpleDateFormat("dd/MM/yyyy").format(uploadedAt)
            }
        }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()?.let { JSONArray(it) },
        if (parcel.readLong() == -1L) null else Date(parcel.readLong()),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readDoubleOrNull(),
        parcel.readDoubleOrNull()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(mediaUrl)
        parcel.writeInt(duration)
        parcel.writeString(trimmedAudioUrl)
        parcel.writeString(draggableTexts?.toString())
        parcel.writeLong(uploadedAt?.time ?: -1)
        parcel.writeString(userName)
        parcel.writeString(userProfilePicture)
        parcel.writeString(city)
        parcel.writeString(country)
        parcel.writeDoubleOrNull(latitude)
        parcel.writeDoubleOrNull(longitude)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Story> {
        override fun createFromParcel(parcel: Parcel): Story = Story(parcel)
        override fun newArray(size: Int): Array<Story?> = arrayOfNulls(size)
    }
}

fun Parcel.readDoubleOrNull(): Double? {
    val value = readDouble()
    return if (value == Double.NaN) null else value
}

fun Parcel.writeDoubleOrNull(value: Double?) {
    if (value != null) writeDouble(value) else writeDouble(Double.NaN)
}
