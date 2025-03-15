package com.muhaimen.arenax.dataClasses

data class Team(
    val teamName: String,
    val gameName: String,
    val teamDetails: String,
    val teamLocation: String,
    val teamEmail: String,
    val teamCaptain: String,
    val teamTagLine: String,
    val teamAchievements: String,
    val teamLogo: String,
    val teamMembers: List<String>? = null
) {
    constructor() : this("", "", "", "", "", "", "", "", "", null)
}

