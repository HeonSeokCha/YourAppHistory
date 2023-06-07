package com.example.yourapphistory

data class AppUsageInfo(
    val packageName: String,
    val beginTime: Long = 0L,
    val endTime: Long = 0L
)
