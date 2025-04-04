package com.chs.yourapphistory.presentation.screen.used_app_list

import com.chs.yourapphistory.domain.model.AppInfo
import java.time.LocalDate

sealed class UsedAppEvent {

    data object Idle : UsedAppEvent()

    sealed class GetUsageEvent(val name: String): UsedAppEvent() {
        data object AppUsageEvent : GetUsageEvent("앱 사용량")
        data object AppForegroundUsageEvent : GetUsageEvent("앱 포그라운드 사용량")
        data object AppNotifyEvent : GetUsageEvent("앱 알림 횟수")
        data object AppLaunchEvent : GetUsageEvent("앱 실행 횟수")
    }

    sealed class Click {

        data class Application(
            val appInfo: AppInfo,
            val targetDate: Long
        ) : UsedAppEvent()

        data object Sort : UsedAppEvent()
        data class ChangeDate(val date: LocalDate) : UsedAppEvent()
        data object RefreshAppUsageInfo : UsedAppEvent()
    }

    sealed class Load {
        data object Appending : UsedAppEvent()
        data object Complete : UsedAppEvent()
        data object Error : UsedAppEvent()
    }
}