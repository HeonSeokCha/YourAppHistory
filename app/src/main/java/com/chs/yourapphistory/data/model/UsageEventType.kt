package com.chs.yourapphistory.data.model

sealed class UsageEventType(val name: String) {
    data object AppUsageEvent : UsageEventType("앱 사용량")
    data object AppForegroundUsageEvent : UsageEventType("앱 포그라운드 사용량")
    data object AppNotifyEvent : UsageEventType("앱 알림 횟수")
    data object AppLaunchEvent : UsageEventType("앱 실행 횟수")
}