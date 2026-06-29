package com.chs.yourapphistory.domain.model

data class AppUsageInfo(
    val packageName: String,
    val beginUseTime: Long,
    val endUseTime: Long = 0L,
)
