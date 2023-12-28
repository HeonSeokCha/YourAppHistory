package com.chs.yourapphistory.presentation.screen.used_app_list

import com.chs.yourapphistory.domain.model.AppInfo
import java.time.LocalDate

data class UsedAppListState(
    val localDateList: List<LocalDate> = emptyList(),
    val targetDate: LocalDate = LocalDate.now(),
    val appInfoList: List<Pair<AppInfo, String>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)