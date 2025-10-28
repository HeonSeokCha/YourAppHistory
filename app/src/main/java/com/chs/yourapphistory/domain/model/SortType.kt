package com.chs.yourapphistory.domain.model

enum class SortType(val title: String) {
    UsageEvent("앱 사용량"),
    ForegroundUsageEvent("앱 포그라운드 사용량"),
    NotifyEvent("앱 알림 횟수"),
    LaunchEvent("앱 실행 횟수"),
}