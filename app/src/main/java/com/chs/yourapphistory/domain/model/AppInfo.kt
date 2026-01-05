package com.chs.yourapphistory.domain.model

import android.graphics.Bitmap

data class AppInfo(
    override val packageName: String,
    override val icon: Bitmap?,
    override val label: String,
) : BaseAppInfo
