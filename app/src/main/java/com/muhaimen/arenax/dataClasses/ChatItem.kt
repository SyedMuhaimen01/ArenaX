package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateFormat
import java.util.*

data class ChatItem(
    val chatId: String = UUID.randomUUID().toString(), // Unique ID for each chat item
    val senderId: String = "", // Provide default value
    val receiverId: String = "", // Provide default value
    val message: String = "",
    val time: Long = System.currentTimeMillis(),
    val contentType: ContentType = ContentType.TEXT,
    val contentUri: String? = null,
    val lastReadTime: Long = 0L, // To keep track of when the message was last read
    var isRead: Boolean = false, // Whether the message has been read
    val attachmentUrl: String? = null, // URL for any attached files
    var editedTime: Long? = null, // Timestamp for when the message was last edited
    var isEdited: Boolean = false // Flag to indicate if the message was edited
) : Parcelable {

    // Enum for the type of content (text, image, video)
    enum class ContentType {
        TEXT, IMAGE, VIDEO
    }

    // Computed property to format sent time for display
    val formattedSentTime: String
        get() = DateFormat.format("hh:mm a", time).toString()

    // Computed property to format edited time for display
    val formattedEditedTime: String?
        get() = editedTime?.let { DateFormat.format("hh:mm a", it).toString() }

    // No-argument constructor is automatically provided by default values.
    // The Parcel constructor is necessary for Parcelable implementation.
    constructor(parcel: Parcel) : this(
        chatId = parcel.readString() ?: UUID.randomUUID().toString(),
        senderId = parcel.readString() ?: "",
        receiverId = parcel.readString() ?: "",
        message = parcel.readString() ?: "",
        time = parcel.readLong(),
        contentType = ContentType.valueOf(parcel.readString() ?: ContentType.TEXT.name),
        contentUri = parcel.readString(),
        lastReadTime = parcel.readLong(),
        isRead = parcel.readByte() != 0.toByte(),
        attachmentUrl = parcel.readString(),
        editedTime = parcel.readValue(Long::class.java.classLoader) as? Long,
        isEdited = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(chatId)
        parcel.writeString(senderId)
        parcel.writeString(receiverId)
        parcel.writeString(message)
        parcel.writeLong(time)
        parcel.writeString(contentType.name)
        parcel.writeString(contentUri)
        parcel.writeLong(lastReadTime)
        parcel.writeByte(if (isRead) 1 else 0)
        parcel.writeString(attachmentUrl)
        parcel.writeValue(editedTime)
        parcel.writeByte(if (isEdited) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ChatItem> {
        override fun createFromParcel(parcel: Parcel): ChatItem = ChatItem(parcel)
        override fun newArray(size: Int): Array<ChatItem?> = arrayOfNulls(size)
    }
}
