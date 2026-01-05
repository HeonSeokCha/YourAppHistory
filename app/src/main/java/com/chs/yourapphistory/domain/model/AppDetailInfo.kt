package com.chs.yourapphistory.domain.model

import android.graphics.Bitmap
import java.time.LocalDateTime

data class AppDetailInfo(
    override val packageName: String,
    override val icon: Bitmap?,
    override val label: String,
    val firstInstallTime: LocalDateTime,
    val lastUpdateTime: LocalDateTime,
    val installProvider: String?,
    val lastUsedTime: LocalDateTime?,
    val lastForegroundUsedTime: LocalDateTime?
) : BaseAppInfo
