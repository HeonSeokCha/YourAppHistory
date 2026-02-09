package com.chs.yourapphistory.presentation.screen.used_app_list

import com.chs.yourapphistory.domain.model.AppInfo
import java.time.LocalDate

sealed interface UsedAppEffect {
    data class NavigateAppDetail(val appInfo: AppInfo, val targetDate: Long) : UsedAppEffect
}