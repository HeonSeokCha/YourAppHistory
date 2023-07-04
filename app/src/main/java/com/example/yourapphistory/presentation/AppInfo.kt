package com.example.yourapphistory.presentation

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val todayUsageTime: Long
)