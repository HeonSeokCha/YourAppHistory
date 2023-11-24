package com.example.yourapphistory.domain.model

import android.graphics.Bitmap

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap? = null
)
