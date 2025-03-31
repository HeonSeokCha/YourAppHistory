package com.chs.yourapphistory.common

import android.app.usage.UsageEvents
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppEvent
import java.time.format.DateTimeFormatter

object Constants {
    const val FIRST_COLLECT_DAY: Long = 8L
    const val PAGING_DAY: Long = 7L
    const val PAGING_WEEK: Long = 2L
    val SIMPLE_HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("a h")
    val SIMPLE_HOUR_FORMAT_SIMPLE: DateTimeFormatter = DateTimeFormatter.ofPattern("h시")
    val DATE_NAME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)")
    val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일")
    val YEAR_DATE_FORMAT_NAME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)")
    val YEAR_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")

    const val TEXT_TITLE_PREVIEW: String = "Title Preview"
    const val NUMBER_LOADING_COUNT: Int = 1
    const val TAG_WORKER_NAME: String = "app_worker"
    const val PREF_NAME: String = "YourAppPref"
    val PREF_KEY_FIRST_DATE: Preferences.Key<Long> = longPreferencesKey("first_date")

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
        UsedAppEvent.GetUsageEvent.AppUsageEvent,
        UsedAppEvent.GetUsageEvent.AppForegroundUsageEvent,
        UsedAppEvent.GetUsageEvent.AppNotifyEvent,
        UsedAppEvent.GetUsageEvent.AppLaunchEvent
    )
}