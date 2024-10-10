package com.muhaimen.arenax.dataClasses

import android.graphics.Color

data class DraggableText(
    val content: String, // The text content
    val x: Float, // X-coordinate for the position of the text
    val y: Float, // Y-coordinate for the position of the text
    val backgroundColor: Int? = null, // Use Int to store color value (ARGB format)
    val textColor: Int? = null // Use Int to store text color value (ARGB format)
) {
    fun getBackgroundColor(): Int {
        return backgroundColor ?: Color.TRANSPARENT // Return transparent if no color is set
    }

    fun getTextColor(): Int {
        return textColor ?: Color.BLACK // Return black if no color is set
    }
}
