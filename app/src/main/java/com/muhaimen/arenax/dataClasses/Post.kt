package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable

data class Post(
    val postId: Int,                       // Unique ID of the post
    val postContent: String?,              // Content of the post (can be null if not provided)
    val caption: String?,                  // Caption for the post (can be null if not provided)
    val sponsored: Boolean,                // Indicates if the post is sponsored
    val likes: Int,                        // Number of likes on the post
    val comments: Int,                     // Number of comments on the post
    val shares: Int,                       // Number of shares of the post
    val clicks: Int,                       // Number of clicks on the post
    val city: String?,                     // City where the post was created (can be null if not provided)
    val country: String?,                  // Country where the post was created (can be null if not provided)
    val trimmedAudioUrl: String?,          // URL of the trimmed audio (can be null if not provided)
    val createdAt: String,                 // Timestamp when the post was created
    val userFullName: String,              // Full name of the user who posted
    val userProfilePictureUrl: String?,    // Profile picture URL of the user
    var commentsData: List<Comment>?,      // List of comments associated with the post (can be null or empty)
    var isLikedByUser: Boolean = false    // Indicates whether the post is liked by the user (default is false)
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
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString().toString(),
        parcel.readString(),
        parcel.createTypedArrayList(Comment.CREATOR),
        parcel.readByte() != 0.toByte() // Read the `isLikedByUser` value from the Parcel
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
        parcel.writeString(trimmedAudioUrl)
        parcel.writeString(createdAt)
        parcel.writeString(userFullName)
        parcel.writeString(userProfilePictureUrl)
        parcel.writeTypedList(commentsData)
        parcel.writeByte(if (isLikedByUser) 1 else 0)  // Write the `isLikedByUser` value to the Parcel
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}

data class Comment(
    val commentId: Int,                    // Unique ID for the comment
    val commentText: String,               // The actual comment text
    val createdAt: String,                 // Timestamp when the comment was created
    val commenterName: String,             // Name of the user who commented
    val commenterProfilePictureUrl: String? // Profile picture URL of the commenter (can be null)
) : Parcelable {

    constructor(parcel: Parcel) : this(
        commentId = parcel.readInt(),
        commentText = parcel.readString() ?: "",
        createdAt = parcel.readString() ?: "",
        commenterName = parcel.readString() ?: "",
        commenterProfilePictureUrl = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(commentId)
        parcel.writeString(commentText)
        parcel.writeString(createdAt)
        parcel.writeString(commenterName)
        parcel.writeString(commenterProfilePictureUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }
    }
}

data class Likes(
    val postId: Int,
    val userId: String
)
