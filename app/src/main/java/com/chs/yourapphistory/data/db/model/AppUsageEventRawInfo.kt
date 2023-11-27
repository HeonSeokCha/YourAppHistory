package com.chs.yourapphistory.data.db.model

data class AppUsageEventRawInfo(
    val packageName: String,
    val className: String?,
    val eventType: Int,
    val eventTime: Long
)
