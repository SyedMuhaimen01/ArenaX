package com.muhaimen.arenax.dataClasses

import com.jjoe64.graphview.series.DataPoint

data class AnalyticsData(
    val gameName: String,
    val totalHours: Int,
    val iconResId: String,
    val graphData: List<DataPoint>

)
