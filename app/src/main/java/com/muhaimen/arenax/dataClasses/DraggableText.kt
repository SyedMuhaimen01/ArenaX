package com.muhaimen.arenax.dataClasses

import android.content.res.ColorStateList

data class DraggableText(
    val content: String,
    val x: Float, // X-coordinate for the position
    val y: Float,  // Y-coor
    val backgroundColor: ColorStateList?,
    val textColor: ColorStateList
)
