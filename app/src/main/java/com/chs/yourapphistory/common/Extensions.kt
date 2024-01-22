package com.chs.yourapphistory.common

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import com.chs.yourapphistory.domain.model.AppBaseUsageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.hours

fun getUntilDateList(targetDate: LocalDate): List<LocalDate> {
    return if (targetDate == LocalDate.now()) {
        listOf(targetDate)
    } else {
        targetDate.datesUntil(LocalDate.now().plusDays(1L))
            .toList()
            .reversed()
    }
}

fun Int.convert24HourString(isShowAMPM: Boolean): String {
    val localTime: LocalTime = LocalTime.MIDNIGHT
    return localTime.plusHours(this.toLong())
        .format(
            if (isShowAMPM) {
                Constants.SIMPLE_HOUR_FORMAT
            } else {
                Constants.SIMPLE_HOUR_FORMAT_SIMPLE
            }
        )
}

fun Int.convertBetweenHourString(): String {
    return this.convert24HourString(true) +
            " ~ " +
            (this + 1).convert24HourString(false)
}

suspend fun calculateTimeZoneUsage(
    date: LocalDate,
    list: List<AppBaseUsageInfo>
): List<Pair<Int, Long>> {
    val usageMap = object : HashMap<Int, Long>() {
        init {
            for (i in 0..23) {
                put(i, 0L)
            }
        }
    }
    chsLog(list.toString())

    withContext(Dispatchers.Default) {
        list.forEach { appUsageInfo ->
            if (date.dayOfMonth < appUsageInfo.endUseTime.dayOfMonth) {
                usageMap.computeIfPresent(appUsageInfo.beginUseTime.hour) { key, value ->
                    val nextDayStartMilli = date.plusDays(1L).atStartOfDayToMillis()
                    value + (nextDayStartMilli - appUsageInfo.beginUseTime.toMillis())
                }
                return@forEach
            }

            if (date.dayOfMonth > appUsageInfo.beginUseTime.dayOfMonth) {
                usageMap.computeIfPresent(appUsageInfo.endUseTime.hour) { key, value ->
                    val dayStartMilli = date.atStartOfDayToMillis()
                    value + (appUsageInfo.endUseTime.toMillis() - dayStartMilli)
                }
                return@forEach
            }

            usageMap.computeIfPresent(appUsageInfo.beginUseTime.hour) { key, value ->
                if (appUsageInfo.beginUseTime.hour < appUsageInfo.endUseTime.hour) {
                    for (i in appUsageInfo.beginUseTime.hour + 1..appUsageInfo.endUseTime.hour) {
                        val targetHour = date.atStartOfDay().plusHours(i.toLong())
                        usageMap.computeIfPresent(i) { key1, value1 ->
                            if (i == appUsageInfo.endUseTime.hour) {
                                value1 + (appUsageInfo.endUseTime.toMillis() - targetHour.toMillis())
                            } else {
                                1.hours.inWholeMilliseconds
                            }
                        }
                    }
                    val nextHour =
                        date.atStartOfDay().plusHours((appUsageInfo.beginUseTime.hour + 1).toLong())
                    value + (nextHour.toMillis() - appUsageInfo.beginUseTime.toMillis())
                } else {
                    value + (appUsageInfo.endUseTime.toMillis() - appUsageInfo.beginUseTime.toMillis())
                }
            }
        }
    }
    return usageMap.toList()
}

fun Long?.isZero(): Boolean {
    return this == null || this == 0L
}

fun Long.convertToRealUsageMinutes(): String {
    return "${(this / 1000) / 60 % 60}분"
}

fun Long.convertToRealUsageTime(): String {
    val hour: Long = (this / 1000) / 60 / 60 % 24
    val minutes: Long = (this / 1000) / 60 % 60
    val second: Long = (this / 1000) % 60
    val milliSec: Long = (this / 1000)
    var result: String = ""

    if (hour != 0L) {
        result += "${hour}시간 "
    }

    if (minutes != 0L) {
        result += "${minutes}분 "
    }

    if (second != 0L) {
        result += "${second}초"
    } else {
        if (hour == 0L && minutes == 0L) {
            result = if (milliSec != 0L) {
                "1초 미만"
            } else {
                "0초"
            }
        }
    }
    return result
}


fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()): Long {
    return atZone(zone)?.toInstant()?.toEpochMilli() ?: 0L
}

fun LocalDate.toMillis(): Long {
    return this.atStartOfDay().toMillis()
}

fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

fun LocalDate.atStartOfDayToMillis(): Long {
    return this.atStartOfDay().toMillis()
}

fun LocalDate.atEndOfDayToMillis(): Long {
    return this.atTime(LocalTime.MAX).toMillis()
}

fun calculateScale(viewHeightPx: Int, values: List<Long>): Double {
    return values.maxOrNull()?.let { max ->
        viewHeightPx.times(0.8).div(max)
    } ?: 1.0
}

fun getUsagePermission(context: Context): Boolean {
    val appOps: AppOpsManager =
        context.getSystemService(ComponentActivity.APP_OPS_SERVICE) as AppOpsManager
    return try {
        val mode: Int =
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        mode == AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        false
    }
}

fun chsLog(value: String) {
    Log.e("CHS_LOG", value)
}