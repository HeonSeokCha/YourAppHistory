package com.chs.yourapphistory.presentation.screen.used_app_list

import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.UsageEventType

data class UsedAppListState(
    val isLoading: Boolean = true,
    val isAppending: Boolean = false,
    val isShowFilterDialog: Boolean = false,
    val displayDate: String = "오늘",
    val sortOption: UsageEventType = UsageEventType.UsageEvent,
    val originList: List<Pair<AppInfo, Int>> = emptyList(),
    val list: List<Pair<AppInfo, Int>> = emptyList()
)