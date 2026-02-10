package com.chs.yourapphistory.presentation.screen.used_app_list

import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.SortType
import java.time.LocalDate

sealed interface UsedAppIntent {
    data class ClickAppInfo(val appInfo: AppInfo) : UsedAppIntent
    data class ChangeSearchQuery(val query: String) : UsedAppIntent
    data class OnChangeSort(val sort: SortType) : UsedAppIntent
    data class OnShowSortDialog(val value: Boolean) : UsedAppIntent
    data class ChangeDate(val date: LocalDate) : UsedAppIntent
    data object Error : UsedAppIntent
}