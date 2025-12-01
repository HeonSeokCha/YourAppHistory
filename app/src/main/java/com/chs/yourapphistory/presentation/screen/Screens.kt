package com.chs.yourapphistory.presentation.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface MainScreens: NavKey {
    @Serializable
    data object ScreenWelcome : MainScreens

    @Serializable
    data object ScreenUsedAppList : MainScreens

    @Serializable
    data class ScreenAppUsageDetail(
        val targetPackageName: String,
        val targetLabelName: String,
        val targetDate: Long
    ) : MainScreens
}