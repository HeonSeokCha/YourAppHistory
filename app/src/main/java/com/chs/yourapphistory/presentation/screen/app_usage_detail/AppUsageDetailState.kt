package com.chs.yourapphistory.presentation.screen.app_usage_detail

import com.chs.yourapphistory.domain.model.AppUsageInfo
import java.time.LocalDate

data class AppUsageDetailState(
    val targetDate: LocalDate = LocalDate.now(),
    val dayUsageList: List<AppUsageInfo> = emptyList()
)
