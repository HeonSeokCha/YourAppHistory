package com.chs.yourapphistory.presentation.screen.total_summary

import java.time.LocalDate

data class TotalSummaryState(
    val loading: Boolean = false,
    val minDate: LocalDate = LocalDate.now(),
    val displayDate: LocalDate = LocalDate.now(),
    val dateList: List<List<LocalDate>> = listOf(),
    val dateIdx: Pair<Int, Int> = (0 to 0),
    val dateCurrentPage: Int = 0
)
