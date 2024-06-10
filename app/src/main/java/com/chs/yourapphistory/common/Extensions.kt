package com.chs.yourapphistory.common

import android.app.AppOpsManager
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.os.Process
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.hours

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
            (this + 1).convert24HourString(false) + " "
}

fun Map<Long, Long>.toConvertDayUsedTime(targetDate: LocalDate): Int {
    return this.map {
        it.key.toLocalDateTime() to it.value.toLocalDateTime()
    }.sumOf {
        val (begin, end) = it.first to it.second
        return@sumOf if (begin.dayOfMonth < end.dayOfMonth) {
            if (targetDate.dayOfMonth == end.dayOfMonth) {
                end.toLocalDate().atStartOfDayToMillis() - begin.toMillis()
            } else {
                targetDate.atStartOfDayToMillis() - begin.toMillis()
            }
        } else {
            end.toMillis() - begin.toMillis()
        }
    }.toInt()
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

    withContext(Dispatchers.Default) {
        list.forEach { appUsageInfo ->
        }
    }
    return usageMap.toList()
}

fun Long?.isZero(): Boolean {
    return this == null || this == 0L
}

fun Long.convertToRealUsageMinutes(): String {
    return "${(this / 1000) / 60}분"
}

fun Int.convertToRealUsageTime(): String {
    val hour: Int = (this / 1000) / 60 / 60 % 24
    val minutes: Int = (this / 1000) / 60 % 60
    val second: Int = (this / 1000) % 60

    return when {
        hour != 0 -> {
            if (minutes != 0) {
                String.format("%02d시간 %02d분", hour, minutes)
            } else {
                String.format("%02d시간", hour, minutes)
            }
        }

        minutes != 0 -> {
            if (second != 0) {
                String.format("%02d분 %02d초", minutes, second)
            } else {
                String.format("%02d분", minutes, second)
            }
        }

        else -> {
            if (second == 0) {
                String.format("%01d초", second)
            } else {
                String.format("%02d초", second)
            }
        }
    }
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

fun Long.getDayOfMonth(): Int {
    return this.toLocalDate().dayOfMonth
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
        context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
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

fun LocalDate.reverseDateUntil(targetDate: LocalDate): List<LocalDate> {
    return this.datesUntil(targetDate)
        .toList()
        .reversed()
}

fun chsLog(value: String) {
    Log.e("CHS_LOG", value)
}
