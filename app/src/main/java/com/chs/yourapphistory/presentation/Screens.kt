package com.chs.yourapphistory.presentation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object ScreenWelcome : Screen()
    @Serializable
    data object ScreenUsedAppList : Screen()
    @Serializable
    class ScreenAppUsageDetail(val packageName: String, val targetDate: Long) : Screen()
}
