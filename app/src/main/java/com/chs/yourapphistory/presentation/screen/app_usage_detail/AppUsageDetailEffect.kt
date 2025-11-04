package com.chs.yourapphistory.presentation.screen.app_usage_detail

sealed interface AppUsageDetailEffect {
    data object ShowPagingError : AppUsageDetailEffect
}