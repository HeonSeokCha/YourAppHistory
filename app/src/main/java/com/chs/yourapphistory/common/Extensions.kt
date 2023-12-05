package com.chs.yourapphistory.common

import android.util.Log
import com.chs.yourapphistory.domain.model.AppUsageInfo
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun getUntilDateList(targetDate: LocalDate): List<LocalDate> {
    return if (targetDate == LocalDate.now()) {
        listOf(targetDate)
    } else {
        targetDate.datesUntil(LocalDate.now().plusDays(1L))
            .toList()
            .sortedByDescending { it }
    }
}

fun calculateSplitHourUsage(list: List<AppUsageInfo>): List<Pair<Int, Long>> {
    val usageMap = object : HashMap<Int, Long>() {
        init {
            repeat(24) {
                put(it, 0L)
            }
        }
    }

    list.forEach { appUsageInfo ->
        usageMap.computeIfPresent(appUsageInfo.beginUseTime.hour) { key, value ->
            if (appUsageInfo.beginUseTime.hour < appUsageInfo.endUseTime.hour) {
                val nextHourTime = LocalTime.MIN.plusHours(appUsageInfo.endUseTime.hour.toLong())
                    .atDate(appUsageInfo.endUseTime.toLocalDate())
                usageMap.computeIfPresent(appUsageInfo.endUseTime.hour) { key, value ->
                    value + (appUsageInfo.endUseTime.toMillis() - nextHourTime.toMillis())
                }
                (nextHourTime.toMillis() - appUsageInfo.beginUseTime.toMillis())
            } else {
                (appUsageInfo.endUseTime.toMillis() - appUsageInfo.beginUseTime.toMillis())
            }
        }
    }

    val a =  usageMap.toList().sortedBy { it.first }
    a.forEach {
        Log.e("ABCD", "${it.first} -> ${it.second}")
    }
    return a
}

fun Long?.isZero(): Boolean {
    return this == null || this == 0L
}

fun Long.convertToRealUsageTime(): String {
    val hour: Long = (this / 1000) / 60 / 60 % 24
    val minutes: Long = (this / 1000) / 60 % 60
    val second: Long = (this / 1000) % 60
    return if (hour == 0L) {
        if (minutes == 0L) {
            if (second == 0L) {
                "< 1초"
            } else {
                "${second}초"
            }
        } else {
            "${minutes}분 ${second}초"
        }
    } else {
        "${hour}시간 ${minutes}분 ${second}초"
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

fun LocalDate.atStartOfDayToMillis(): Long {
    return this.atStartOfDay().toMillis()
}

fun LocalDate.atEndOfDayToMillis(): Long {
    return this.atTime(LocalTime.MAX).toMillis()
}

fun calculateScale(viewHeightPx: Int, values: List<Int>): Double {
    return values.maxOrNull()?.let { max ->
        viewHeightPx.times(0.8).div(max)
    } ?: 1.0
}
