package com.chs.yourapphistory.domain.model

import android.graphics.Bitmap

data class AppInfo(
    override val packageName: String,
    val icon: Bitmap?,
    val label: String,
) : BaseAppInfo
