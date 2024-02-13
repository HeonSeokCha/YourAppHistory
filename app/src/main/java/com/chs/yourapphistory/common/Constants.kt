package com.chs.yourapphistory.common

import android.app.usage.UsageEvents
import java.time.format.DateTimeFormatter

object Constants {
    const val FIRST_COLLECT_DAY: Long = 5L
    const val PAGING_DAY: Long = 3L
    val SIMPLE_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    val SIMPLE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val SIMPLE_HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("a h")
    val SIMPLE_HOUR_FORMAT_SIMPLE: DateTimeFormatter = DateTimeFormatter.ofPattern("h시")
    val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM월 dd일 (E)")
    val SQL_DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    const val KEY_TARGET_DATE: String = "key_target_date"
    const val KEY_TARGET_PACKAGE_NAME: String = "key_target_package_name"

    val APP_USAGE_EVENT_FILTER = listOf(
        UsageEvents.Event.ACTIVITY_RESUMED,
        UsageEvents.Event.ACTIVITY_PAUSED,
        UsageEvents.Event.ACTIVITY_STOPPED,
        UsageEvents.Event.SCREEN_NON_INTERACTIVE,
        UsageEvents.Event.SCREEN_INTERACTIVE
    )

    val APP_FOREGROUND_USAGE_EVENT_FILTER = listOf(
        UsageEvents.Event.FOREGROUND_SERVICE_START,
        UsageEvents.Event.FOREGROUND_SERVICE_STOP
    )
}