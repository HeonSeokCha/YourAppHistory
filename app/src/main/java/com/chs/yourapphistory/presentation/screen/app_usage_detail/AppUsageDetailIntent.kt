package com.chs.yourapphistory.presentation.screen.app_usage_detail

import java.time.LocalDate

sealed interface AppUsageDetailIntent {
    data class OnChangeTargetDate(val date: LocalDate) : AppUsageDetailIntent
    data class OnChangeTargetWeek(val date: LocalDate) : AppUsageDetailIntent
    data object OnChangeViewType : AppUsageDetailIntent
}