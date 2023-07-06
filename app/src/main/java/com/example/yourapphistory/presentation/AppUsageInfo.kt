package com.example.yourapphistory.presentation

data class AppUsageInfo(
    val packageName: String,
    val beginTime: Long,
    val endTime: Long = 0L
)
