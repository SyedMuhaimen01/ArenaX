package com.muhaimen.arenax.userProfile

import com.jjoe64.graphview.series.DataPoint

data class AnalyticsData(
    val gameName: String,
    val totalHours: Int,
    val iconResId: Int,
    val hoursData: List<DataPoint>
)