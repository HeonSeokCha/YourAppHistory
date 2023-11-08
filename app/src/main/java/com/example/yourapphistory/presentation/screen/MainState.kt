package com.example.yourapphistory.presentation.screen

import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import java.time.LocalDate

data class MainState(
    val targetDate: LocalDate = LocalDate.now(),
    val localDateList: List<LocalDate> = emptyList(),
    val appInfoList: List<Pair<AppInfo, List<AppUsageInfo>>> = emptyList(),
    val isLoading: Boolean = false
)