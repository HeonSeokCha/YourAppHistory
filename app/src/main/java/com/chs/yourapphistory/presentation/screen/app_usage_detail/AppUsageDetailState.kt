package com.chs.yourapphistory.presentation.screen.app_usage_detail

import java.time.LocalDate

data class AppUsageDetailState(
    val packageLabel: String = "",
    val isDateLoading: Boolean = false,
    val isWeekLoading: Boolean = false,
    val minDate: LocalDate = LocalDate.now(),
    val displayDate: LocalDate = LocalDate.now(),
    val displayWeek: List<LocalDate> = listOf(),
    val isWeeklyMode: Boolean = false,
    val dateList: List<List<LocalDate>> = listOf(),
    val dateIdx: Pair<Int, Int> = (0 to 0),
    val weekList: List<List<List<LocalDate>>> = listOf(),
    val weekIdx: Pair<Int, Int> = (0 to 0),
)
