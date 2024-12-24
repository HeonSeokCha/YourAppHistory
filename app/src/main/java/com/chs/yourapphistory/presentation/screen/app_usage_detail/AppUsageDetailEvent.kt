package com.chs.yourapphistory.presentation.screen.app_usage_detail

import java.time.LocalDate

sealed interface AppUsageDetailEvent {
    data object OnBackClick : AppUsageDetailEvent
    data class OnChangeTargetDate(val date: LocalDate) : AppUsageDetailEvent
}