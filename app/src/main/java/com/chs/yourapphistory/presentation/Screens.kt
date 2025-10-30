package com.chs.yourapphistory.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ScreenWelcome : NavKey

@Serializable
data object ScreenUsedAppList : NavKey

@Serializable
data class ScreenAppUsageDetail(
    val targetPackageName: String,
    val targetDate: Long
) : NavKey