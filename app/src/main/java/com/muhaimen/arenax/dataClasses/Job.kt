package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable

data class Job(
    var jobId: String,
    var organizationId: String,
    var jobTitle: String,
    var jobType: String,
    var jobLocation: String,
    var jobDescription: String,
    var workplaceType: String,
    var tags: List<String>
) : Parcelable {

    // Constructor to read from Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: listOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(jobId)
        parcel.writeString(organizationId)
        parcel.writeString(jobTitle)
        parcel.writeString(jobType)
        parcel.writeString(jobLocation)
        parcel.writeString(jobDescription)
        parcel.writeString(workplaceType)
        parcel.writeStringList(tags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Job> {
        override fun createFromParcel(parcel: Parcel): Job {
            return Job(parcel)
        }

        override fun newArray(size: Int): Array<Job?> {
            return arrayOfNulls(size)
        }
    }
}
