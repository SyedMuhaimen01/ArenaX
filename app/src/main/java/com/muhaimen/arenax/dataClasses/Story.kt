package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray

data class Story(
    val id: Int,
    val mediaUrl: String,
    val duration: Int,
    val trimmedAudioUrl: String?,
    val draggableTexts: JSONArray? // Keep this as JSONArray
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()?.let { JSONArray(it) } // Deserialize JSONArray from String
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(mediaUrl)
        parcel.writeInt(duration)
        parcel.writeString(trimmedAudioUrl)
        parcel.writeString(draggableTexts?.toString()) // Serialize JSONArray to String
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Story> {
        override fun createFromParcel(parcel: Parcel): Story {
            return Story(parcel)
        }

        override fun newArray(size: Int): Array<Story?> {
            return arrayOfNulls(size)
        }
    }
}


data class StoryWithTimeAgo(
    val story: Story,
    val hoursAgo: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Story::class.java.classLoader)!!,
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(story, flags)
        parcel.writeLong(hoursAgo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StoryWithTimeAgo> {
        override fun createFromParcel(parcel: Parcel): StoryWithTimeAgo {
            return StoryWithTimeAgo(parcel)
        }

        override fun newArray(size: Int): Array<StoryWithTimeAgo?> {
            return arrayOfNulls(size)
        }
    }
}


