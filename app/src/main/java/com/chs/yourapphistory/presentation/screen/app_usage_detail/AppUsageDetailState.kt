package com.chs.yourapphistory.presentation.screen.app_usage_detail

import java.time.LocalDate

data class AppUsageDetailState(
    val datesList: List<LocalDate> = emptyList(),
    val targetDate: LocalDate = LocalDate.now(),
    val targetPackageLabel: String? = null,
    val dayUsageList: List<Pair<Int, Long>> = emptyList(),
    val launchCount: List<Pair<Int, Long>> = emptyList(),
    val notifyCount: List<Pair< Int, Long>> = emptyList(),
    val foregroundUsageList: List<Pair<Int, Long>> = emptyList()
)
