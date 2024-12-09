package com.muhaimen.arenax.dataClasses

data class AnalyticsData(
    val gameName: String,
    val totalHours: Double,
    val iconResId: String,
    val graphData: List<Pair<String, Double>>
)
