package com.chs.yourapphistory.domain.model

import java.time.LocalDateTime

data class AppForegroundUsageInfo(
    val packageName: String,
    val beginTime: LocalDateTime,
    val endTime: LocalDateTime
)