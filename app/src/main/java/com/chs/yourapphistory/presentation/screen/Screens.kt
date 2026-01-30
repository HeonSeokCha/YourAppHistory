package com.chs.yourapphistory.presentation.screen

import androidx.navigation3.runtime.NavKey
import com.chs.yourapphistory.domain.model.SortType
import kotlinx.serialization.Serializable

@Serializable
sealed interface MainScreens: NavKey {
    @Serializable
    data object ScreenWelcome : MainScreens

    @Serializable
    data class ScreenUsedAppList(
        val targetDateMilli: Long,
        val sortType: SortType
    ) : MainScreens

    @Serializable
    data class ScreenAppUsageDetail(
        val targetPackageName: String,
        val targetLabelName: String,
        val targetDateMilli: Long
    ) : MainScreens

    @Serializable
    data object ScreenTotalSummary : MainScreens
}