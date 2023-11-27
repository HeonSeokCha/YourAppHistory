package com.chs.yourapphistory.data.db.model

data class AppUsageRawInfo(
    val packageName: String,
    val beginUseTime: Long,
    val endUseTime: Long = 0L
)
