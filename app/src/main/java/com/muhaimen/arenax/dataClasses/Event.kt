package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable

data class Event(
    var eventId: String,
    var organizationId:String,
    var eventName: String,
    var eventMode: String,
    var platform: String,
    var location: String,
    var eventDescription: String,
    var startDate: String,
    var endDate: String,
    var startTime: String,
    var endTime: String,
    var eventLink: String,
    var eventBanner: String,
) : Parcelable {

    // Write data to Parcel
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

    // Describe contents (usually return 0)
    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: ""
            )
        }

        override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
    }
}
