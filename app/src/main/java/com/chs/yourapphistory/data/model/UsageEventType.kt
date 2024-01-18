package com.chs.yourapphistory.data.model

sealed class UsageEventType {
    data object AppUsageEvent : UsageEventType()
    data object AppForegroundUsageEvent : UsageEventType()
    data object AppNotifyEvent : UsageEventType()
}