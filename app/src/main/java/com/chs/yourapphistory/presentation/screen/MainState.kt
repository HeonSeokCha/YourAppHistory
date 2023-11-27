package com.chs.yourapphistory.presentation.screen

import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import java.time.LocalDate

data class MainState(
    val targetDate: LocalDate = LocalDate.now(),
    val localDateList: List<LocalDate> = emptyList(),
    val appInfoList: List<Pair<AppInfo, String>> = emptyList(),
    val targetPackageName: String? = null,
    val appUsageList: List<AppUsageInfo> = emptyList(),
    val isLoading: Boolean = false
)