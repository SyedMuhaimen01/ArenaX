package com.muhaimen.arenax.dataClasses

data class GameStats(
    val userGameId: Int,
    val gameName: String,
    val packageName: String,
    val logoUrl: String,
    val totalHours: Int,
    val avgPlaytime: Int,
    val peakPlaytime: Int
)
