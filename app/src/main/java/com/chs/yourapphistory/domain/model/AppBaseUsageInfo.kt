package com.chs.yourapphistory.domain.model

import java.time.LocalDateTime

sealed class AppBaseUsageInfo(
    open val packageName: String,
    open val beginUseTime: LocalDateTime,
    open val endUseTime: LocalDateTime
) {
    data class AppUsageInfo(
        override val packageName: String,
        override val beginUseTime: LocalDateTime,
        override val endUseTime: LocalDateTime
    ) : AppBaseUsageInfo(packageName, beginUseTime, endUseTime)


    data class AppForegroundUsageInfo(
        override val packageName: String,
        override val beginUseTime: LocalDateTime,
        override val endUseTime: LocalDateTime
    ) : AppBaseUsageInfo(packageName, beginUseTime, endUseTime)
}