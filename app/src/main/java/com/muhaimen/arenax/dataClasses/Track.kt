package com.muhaimen.arenax.dataClasses

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String,
    val albumName: String,
    val albumId: String,
    val duration: Int,
    val audioUrl: String,
    val albumImage: String,
    val downloadUrl:String
)
