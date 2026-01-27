package com.chs.yourapphistory.presentation.screen.total_summary

import com.chs.yourapphistory.domain.model.SortType


sealed interface TotalSummaryIntent {
    data class OnChangeTargetDateIdx(val idx: Pair<Int, Int>) : TotalSummaryIntent
    data class OnChangeDateCurrentPage(val page: Int) : TotalSummaryIntent
    data class ClickPackageName(
        val packageName: String,
        val label: String,
        val targetDate: Long
    ) : TotalSummaryIntent
    data class ClickUsedAppList(
        val targetDate: Long,
        val sortType: SortType
    ) : TotalSummaryIntent

    data object Loading : TotalSummaryIntent
    data object LoadComplete : TotalSummaryIntent
    data object Error : TotalSummaryIntent
}