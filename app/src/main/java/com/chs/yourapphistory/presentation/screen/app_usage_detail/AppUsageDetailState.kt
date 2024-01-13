package com.chs.yourapphistory.presentation.screen.app_usage_detail

import com.chs.yourapphistory.domain.usecase.GetAppNotifyCountUseCase
import java.time.LocalDate

data class AppUsageDetailState(
    val datesList: List<LocalDate> = emptyList(),
    val targetDate: LocalDate = LocalDate.now(),
    val targetPackageLabel: String? = null,
    val dayUsageList: List<Pair<Int, Long>> = emptyList(),
    val launchCount: List<Pair<Int, Long>> = emptyList(),
    val notifyCount: List<Pair< Int, Long>> = emptyList(),
    val foregroundUSageList: List<Pair<Int, Long>> = emptyList()
)
