package com.chs.yourapphistory.domain.model

data class InCompleteAppUsageInfo(
    val packageName: String,
    val usageType: String,
    val beginUseTime: Long,
    val className: String?,
)
