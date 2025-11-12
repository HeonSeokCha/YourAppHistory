package com.chs.yourapphistory.presentation.screen.app_usage_detail


sealed interface AppUsageDetailIntent {
    data class OnChangeTargetDateIdx(val idx: Pair<Int, Int>) : AppUsageDetailIntent
    data class OnChangeTargetWeekIdx(val idx: Pair<Int, Int>) : AppUsageDetailIntent
    data object OnChangeViewType : AppUsageDetailIntent

    data object Loading : AppUsageDetailIntent
    data object LoadComplete : AppUsageDetailIntent
    data object Error : AppUsageDetailIntent
}