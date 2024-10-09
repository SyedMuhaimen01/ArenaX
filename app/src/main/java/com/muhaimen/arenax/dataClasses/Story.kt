package com.muhaimen.arenax.dataClasses

import org.json.JSONArray

data class Story(
    val id: Int,
    val mediaUrl: String,
    val duration: Int,
    val trimmedAudioUrl: String?,
    val draggableTexts: JSONArray?
)