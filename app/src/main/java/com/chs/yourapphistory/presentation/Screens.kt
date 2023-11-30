package com.chs.yourapphistory.presentation

sealed class Screen(
    val route: String
) {
    data object ScreenWelcome : Screen("welcome_screen")
    data object ScreenUsedAppList : Screen("used_app_list_screen")
    data object ScreenAppUsageDetail : Screen("app_usage_detail_screen")
}
