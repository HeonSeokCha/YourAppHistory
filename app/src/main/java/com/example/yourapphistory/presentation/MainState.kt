package com.example.yourapphistory.presentation

import java.time.LocalDate

data class MainState(
    val targetDate: LocalDate? = null,
    val localDateList: List<LocalDate> = emptyList(),
    val appInfoList: List<AppInfo> = emptyList()
)