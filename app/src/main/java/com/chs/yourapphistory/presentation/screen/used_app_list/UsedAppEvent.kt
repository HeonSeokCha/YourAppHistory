package com.chs.yourapphistory.presentation.screen.used_app_list


sealed interface UsedAppEvent {
    sealed class GetUsageEvent(val name: String): UsedAppEvent {
        data object AppUsageEvent : GetUsageEvent("앱 사용량")
        data object AppForegroundUsageEvent : GetUsageEvent("앱 포그라운드 사용량")
        data object AppNotifyEvent : GetUsageEvent("앱 알림 횟수")
        data object AppLaunchEvent : GetUsageEvent("앱 실행 횟수")
    }
    data object RefreshAppUsageInfo : UsedAppEvent
}