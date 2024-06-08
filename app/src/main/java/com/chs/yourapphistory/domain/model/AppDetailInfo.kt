package com.chs.yourapphistory.domain.model

data class AppDetailInfo(
    override val packageName: String,
    val foregroundUsageInfo: List<Pair<Int, Int>>,
    val usageInfo: List<Pair<Int, Int>>,
    val notifyInfo: List<Pair<Int, Int>>,
    val launchCountInfo: Int
) : BaseAppInfo
