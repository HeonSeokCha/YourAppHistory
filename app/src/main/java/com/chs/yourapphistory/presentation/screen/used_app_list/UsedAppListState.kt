package com.chs.yourapphistory.presentation.screen.used_app_list

import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.SortType

data class UsedAppListState(
    val isLoading: Boolean = true,
    val isAppending: Boolean = false,
    val isShowFilterDialog: Boolean = false,
    val displayDate: String = "오늘",
    val sortOption: SortType = SortType.UsageEvent,
    val searchQuery: String = "",
    val list: List<Pair<AppInfo, Int>> = emptyList()
)