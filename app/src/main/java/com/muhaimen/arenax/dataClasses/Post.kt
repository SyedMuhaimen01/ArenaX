package com.muhaimen.arenax.dataClasses


data class Post(
    val postId: Int,               // Unique ID of the post
    val postContent: String?,      // Content of the post (can be null if not provided)
    val caption: String?,          // Caption for the post (can be null if not provided)
    val sponsored: Boolean,        // Indicates if the post is sponsored
    val likes: Int,                // Number of likes on the post
    val comments: Int,             // Number of comments on the post
    val shares: Int,               // Number of shares of the post
    val clicks: Int,               // Number of clicks on the post
    val city: String?,             // City where the post was created (can be null if not provided)
    val country: String?,          // Country where the post was created (can be null if not provided)
    val trimmedAudioUrl: String?,  // URL of the trimmed audio (can be null if not provided)
    val createdAt: String          // Timestamp when the post was created
)
