package com.chs.yourapphistory.common

import android.app.usage.UsageEvents
import com.chs.yourapphistory.data.model.UsageEventType
import java.time.format.DateTimeFormatter

object Constants {
    const val FIRST_COLLECT_DAY: Long = 3L
    const val PAGING_DAY: Long = 3L
    val SIMPLE_HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("a h")
    val SIMPLE_HOUR_FORMAT_SIMPLE: DateTimeFormatter = DateTimeFormatter.ofPattern("h시")
    val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM월 dd일 (E)")

    const val KEY_TARGET_DATE: String = "targetDate"
    const val KEY_TARGET_PACKAGE_NAME: String = "targetPackageName"
    const val TEXT_TITLE_PREVIEW: String = "Title Preview"
    const val NUMBER_LOADING_COUNT: Int = 6
    const val TAG_WORKER_NAME: String = "app_worker"

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

    val APP_NOTIFY_EVENT_FILTER = listOf(12)

    val USAGE_EVENT_TYPE_LIST = listOf(
        UsageEventType.AppUsageEvent,
        UsageEventType.AppForegroundUsageEvent,
        UsageEventType.AppNotifyEvent,
        UsageEventType.AppLaunchEvent
    )
}