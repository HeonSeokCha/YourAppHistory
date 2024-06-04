package com.chs.yourapphistory.presentation

import java.time.LocalDate

sealed class Screen {
    data object ScreenWelcome : Screen()
    data object ScreenUsedAppList : Screen()
    class ScreenAppUsageDetail(val packageName: String, val targetDate: Long) : Screen()
}
