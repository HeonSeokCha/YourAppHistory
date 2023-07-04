package com.example.yourapphistory.presentation

data class AppUsageInfo(
    val packageName: String,
    val beginTime: Long = 0L,
    val endTime: Long = 0L
)
