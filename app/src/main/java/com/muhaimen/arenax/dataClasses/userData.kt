package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable

data class UserData(
    var userId: String = "",
    var fullname: String = "",
    var email: String = "",
    var dOB: String = "",
    var gamerTag: String = "",
    var profilePicture: String? = null,
    var gender: Gender = Gender.PreferNotToSay,
    var accountVerified: Boolean = false
) : Parcelable {
    // Constructor for Parcel (used for Parcelable)
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // userId
        parcel.readString() ?: "", // fullname
        parcel.readString() ?: "", // email
        parcel.readString() ?: "", // dOB
        parcel.readString() ?: "", // gamerTag
        parcel.readString(),       // profilePicture
        parcel.readSerializable() as Gender, // gender
        parcel.readByte() != 0.toByte() // accountVerified
    )

    // Writing data to Parcel (used for Parcelable)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(fullname)
        parcel.writeString(email)
        parcel.writeString(dOB)
        parcel.writeString(gamerTag)
        parcel.writeString(profilePicture)
        parcel.writeSerializable(gender)
        parcel.writeByte(if (accountVerified) 1 else 0)
    }

    // No special contents, just return 0
    override fun describeContents(): Int {
        return 0
    }

    // Companion object to create instances from Parcel and create an array
    companion object CREATOR : Parcelable.Creator<UserData> {
        override fun createFromParcel(parcel: Parcel): UserData {
            return UserData(parcel)
        }

        override fun newArray(size: Int): Array<UserData?> {
            return arrayOfNulls(size)
        }
    }
}