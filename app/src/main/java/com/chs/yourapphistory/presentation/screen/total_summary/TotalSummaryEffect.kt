package com.chs.yourapphistory.presentation.screen.total_summary

import com.chs.yourapphistory.domain.model.SortType

sealed interface TotalSummaryEffect {
    data class NavigateUsageDetail(val packageName: String) : TotalSummaryEffect
    data class NavigateUsedAppList(val sortType: SortType) : TotalSummaryEffect
}