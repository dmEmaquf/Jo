    package com.glowstudio.android.blindsjn.tempData

    import androidx.compose.ui.graphics.Color

    data class ScheduleInput(
        val title: String = "",
        val color: Color = Color.Blue,
        val startDate: String = "",
        val startTime: String = "",
        val endDate: String = "",
        val endTime: String = "",
        val memo: String = ""
    )
