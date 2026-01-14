package com.chs.yourapphistory.presentation.screen.total_summary



sealed interface TotalSummaryIntent {
    data class OnChangeTargetDateIdx(val idx: Pair<Int, Int>) : TotalSummaryIntent
    data class OnChangeDateCurrentPage(val page: Int) : TotalSummaryIntent
    data class ClickPackageName(val packageName: String) : TotalSummaryIntent

    data object DateLoading : TotalSummaryIntent
    data object DateLoadComplete : TotalSummaryIntent
    data object Error : TotalSummaryIntent
}