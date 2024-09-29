package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable

data class UserData(
    var userId: String,
    var fullname: String,
    var email: String,
    var password: String,
    var dOB: String,
    var gamerTag: String,
    var profilePicture: String?,
    var gender: Gender
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readSerializable() as Gender
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(fullname)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(dOB)
        parcel.writeString(gamerTag)
        parcel.writeString(profilePicture)
        parcel.writeSerializable(gender)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserData> {
        override fun createFromParcel(parcel: Parcel): UserData {
            return UserData(parcel)
        }

        override fun newArray(size: Int): Array<UserData?> {
            return arrayOfNulls(size)
        }
    }
}
