package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable

data class Event(
    var eventId: String,
    var organizationId: String,
    var eventName: String,
    var eventMode: String,
    var platform: String,
    var location: String? = null,
    var eventDescription: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var startTime: String? = null,
    var endTime: String? = null,
    var eventLink: String? = null,
    var eventBanner: String? = null
) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(eventId)
        parcel.writeString(organizationId)
        parcel.writeString(eventName)
        parcel.writeString(eventMode)
        parcel.writeString(platform)
        parcel.writeString(location)
        parcel.writeString(eventDescription)
        parcel.writeString(startDate)
        parcel.writeString(endDate)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeString(eventLink)
        parcel.writeString(eventBanner)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(
                eventId = parcel.readString() ?: "",
                organizationId = parcel.readString() ?: "",
                eventName = parcel.readString() ?: "",
                eventMode = parcel.readString() ?: "",
                platform = parcel.readString() ?: "",
                location = parcel.readString(),
                eventDescription = parcel.readString(),
                startDate = parcel.readString(),
                endDate = parcel.readString(),
                startTime = parcel.readString(),
                endTime = parcel.readString(),
                eventLink = parcel.readString(),
                eventBanner = parcel.readString()
            )
        }

        override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
    }
}
