package com.chs.yourapphistory.presentation.screen.app_usage_detail


sealed interface AppUsageDetailIntent {
    data class OnChangeTargetDateIdx(val idx: Pair<Int, Int>) : AppUsageDetailIntent
    data class OnChangeTargetWeekIdx(val idx: Pair<Int, Int>) : AppUsageDetailIntent
    data class OnChangeDateCurrentPage(val page: Int) : AppUsageDetailIntent
    data class OnChangeWeekCurrentPage(val page: Int) : AppUsageDetailIntent
    data object OnChangeViewType : AppUsageDetailIntent

    data object DateLoading : AppUsageDetailIntent
    data object WeekLoading : AppUsageDetailIntent
    data object DateLoadComplete : AppUsageDetailIntent
    data object WeekLoadComplete : AppUsageDetailIntent
    data object Error : AppUsageDetailIntent
}