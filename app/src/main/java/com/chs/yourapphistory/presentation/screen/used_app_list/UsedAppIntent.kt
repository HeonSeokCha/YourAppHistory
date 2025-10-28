package com.chs.yourapphistory.presentation.screen.used_app_list

import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.SortType
import java.time.LocalDate

sealed interface UsedAppIntent {

    data class ClickAppInfo(val appInfo: AppInfo, val targetDate: LocalDate) : UsedAppIntent

    data class OnChangeSort(val sort: SortType) : UsedAppIntent
    data class OnShowSortDialog(val value: Boolean) : UsedAppIntent
    data class ChangeDate(val date: LocalDate) : UsedAppIntent

    data object Loading : UsedAppIntent
    data object Appending : UsedAppIntent
    data object LoadComplete : UsedAppIntent
    data object AppendComplete : UsedAppIntent
    data object Error : UsedAppIntent
}