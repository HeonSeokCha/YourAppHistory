package com.chs.yourapphistory.domain.model

import android.graphics.Bitmap

interface BaseAppInfo {
    val packageName: String
    val icon: Bitmap?
    val label: String
}