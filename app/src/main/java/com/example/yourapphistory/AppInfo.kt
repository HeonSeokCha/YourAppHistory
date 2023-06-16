package com.example.yourapphistory

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val todayUsageTime: Long
)