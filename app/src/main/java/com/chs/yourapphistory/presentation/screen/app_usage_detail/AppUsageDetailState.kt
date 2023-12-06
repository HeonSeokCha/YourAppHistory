package com.chs.yourapphistory.presentation.screen.app_usage_detail

import java.time.LocalDate

data class AppUsageDetailState(
    val targetDate: LocalDate = LocalDate.now(),
    val dayUsageList: List<Pair<Int, Long>> = emptyList()
)
