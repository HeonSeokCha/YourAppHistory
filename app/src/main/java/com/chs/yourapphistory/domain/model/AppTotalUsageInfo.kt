package com.chs.yourapphistory.domain.model

data class AppTotalUsageInfo(
    override val packageName: String,
    override val label: String,
    val totalUsedInfo: Long
) : BaseAppInfo
