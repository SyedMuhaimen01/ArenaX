package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable

data class pagePost(
    val postId: Int,
    val postContent: String?,
    val caption: String?,
    val sponsored: Boolean,
    var likes: Int,
    val comments: Int,
    val shares: Int,
    val clicks: Int,
    val city: String?,
    val country: String?,
    val createdAt: String,
    val organizationName: String,
    val organizationLogo: String?,
    var commentsData: List<Comment>?,
    var isLikedByUser: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.createTypedArrayList(Comment.CREATOR),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(postId)
        parcel.writeString(postContent)
        parcel.writeString(caption)
        parcel.writeByte(if (sponsored) 1 else 0)
        parcel.writeInt(likes)
        parcel.writeInt(comments)
        parcel.writeInt(shares)
        parcel.writeInt(clicks)
        parcel.writeString(city)
        parcel.writeString(country)
        parcel.writeString(createdAt)
        parcel.writeString(organizationName)
        parcel.writeString(organizationLogo)
        parcel.writeTypedList(commentsData)
        parcel.writeByte(if (isLikedByUser) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<pagePost> {
        override fun createFromParcel(parcel: Parcel): pagePost {
            return pagePost(parcel)
        }

        override fun newArray(size: Int): Array<pagePost?> {
            return arrayOfNulls(size)
        }
    }
}
