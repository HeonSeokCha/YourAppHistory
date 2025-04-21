package com.chs.yourapphistory.common

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.os.Process
import android.util.Log
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.stream.Collectors
import kotlin.time.Duration.Companion.days
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

        if (targetDate > begin.toLocalDate()) {
            if (targetDate < end.toLocalDate()) {
                return@sumOf 1.days.inWholeMilliseconds
            }
            return@sumOf end.toMillis() - targetDate.atStartOfDayToMillis()
        }

        if (targetDate < end.toLocalDate()) {
            return@sumOf targetDate.plusDays(1L).atStartOfDayToMillis() - begin.toMillis()
        }

        end.toMillis() - begin.toMillis()
    }.toInt()
}

fun Long?.isZero(): Boolean = this == null || this == 0L

fun Int?.isZero(): Boolean = this == null || this == 0

fun Int.convertToRealUsageMinutes(): String {
    return "${(this / 1000) / 60}분"
}

@SuppressLint("DefaultLocale")
fun Int.convertToRealUsageHour(): String {
    val hour: Int = (this / 1000) / 60 / 60
    val minutes: Int = (this / 1000) / 60 % 60

    if (hour == 0) return this.convertToRealUsageMinutes()

    return if (minutes == 0) {
        String.format("%d시간", hour, minutes)
    } else {
        String.format("%d시간 %d분", hour, minutes)
    }
}

@SuppressLint("DefaultLocale")
fun Int.convertToRealUsageTime(): String {
    val hour: Int = (this / 1000) / 60 / 60
    val minutes: Int = (this / 1000) / 60 % 60
    val second: Int = (this / 1000) % 60

    return when {
        hour != 0 -> {
            if (minutes != 0) {
                String.format("%d시간 %d분", hour, minutes)
            } else {
                String.format("%d시간", hour, minutes)
            }
        }

        minutes != 0 -> {
            if (second != 0) {
                String.format("%d분 %d초", minutes, second)
            } else {
                String.format("%d분", minutes, second)
            }
        }

        else -> {
            if (second == 0) {
                String.format("%d초", second)
            } else {
                String.format("%d초", second)
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
    return this.datesUntil(targetDate.plusDays(1L))
        .collect(Collectors.toList())
        .reversed()
}

internal fun calcHourUsageList(
    targetDate: LocalDate,
    list: Map<Long, Long>
): List<Pair<Int, Int>> {
    val usageMap = object : LinkedHashMap<Int, Long>() {
        init {
            for (i in 0..23) {
                put(i, 0L)
            }
        }
    }

    list.forEach {
        val (begin, end) = it.key.toLocalDateTime() to it.value.toLocalDateTime()

        if (targetDate < end.toLocalDate()) {
            val targetDateZeroHour = targetDate.atStartOfDay()
            if (targetDate > begin.toLocalDate()) {
                usageMap.keys.forEach {
                    usageMap.computeIfPresent(it) { _, value ->
                        1.hours.inWholeMilliseconds
                    }
                }

                return@forEach
            }

            for (i in begin.hour..23) {
                usageMap.computeIfPresent(i) { _, value ->
                    val calc = if (i == begin.hour) {
                        targetDateZeroHour.plusHours(i + 1L).toMillis() - begin.toMillis()
                    } else {
                        1.hours.inWholeMilliseconds
                    }

                    value + calc
                }
                targetDateZeroHour.minusHours(i.toLong())
            }
            return@forEach
        }

        if (targetDate > begin.toLocalDate()) {
            val targetDateZeroHour = targetDate.atStartOfDay()
            for (i in 0..end.hour) {
                usageMap.computeIfPresent(i) { key, value ->
                    val calc = if (i == end.hour) {
                        end.toMillis() - targetDateZeroHour.plusHours(i.toLong()).toMillis()
                    } else {
                        1.hours.inWholeMilliseconds
                    }
                    value + calc
                }
                targetDateZeroHour.minusHours(i.toLong())
            }

            return@forEach
        }

        usageMap.computeIfPresent(begin.hour) { key, value ->
            if (begin.hour < end.hour) {
                val targetDateZeroHour = targetDate.atStartOfDay()
                for (i in (begin.hour + 1)..end.hour) {
                    val targetHour = targetDateZeroHour.plusHours(i.toLong())
                    usageMap.computeIfPresent(i) { _, value1 ->
                        if (i == end.hour) {
                            value1 + (end.toMillis() - targetHour.toMillis())
                        } else {
                            1.hours.inWholeMilliseconds
                        }
                    }
                    targetDateZeroHour.minusHours(i.toLong())
                }
                val nextHour = targetDate.atStartOfDay().plusHours((begin.hour + 1).toLong())
                value + (nextHour.toMillis() - begin.toMillis())
            } else {
                value + (end.toMillis() - begin.toMillis())
            }
        }
    }

    return usageMap.toList().map { it.first to it.second.toInt() }
}

internal fun calcHourUsageList(list: List<Long>): List<Pair<Int, Int>> {
    val usageMap = object : HashMap<Int, Long>() {
        init {
            for (i in 0..23) {
                put(i, 0L)
            }
        }
    }

    list.forEach {
        val begin = it.toLocalDateTime()

        usageMap.computeIfPresent(begin.hour) { key, value ->
            value + 1
        }
    }

    return usageMap.toList().map { it.first to it.second.toInt() }
}

@JvmName("calcDayUsedFromUsage")
internal fun calcDayUsedList(
    dateRangeList: List<LocalDate>,
    list: Map<LocalDate, Map<Long, Long>>
): List<Pair<LocalDate, Int>> {
    val usageMap = object : LinkedHashMap<Int, Int>() {
        init {
            put(7, 0)
            for (i in 1..6) {
                put(i, 0)
            }
        }
    }

    list.forEach { mapInfo ->
        val date = mapInfo.key

        usageMap.computeIfPresent(date.dayOfWeek.value) { _, value ->
            value + mapInfo.value.toConvertDayUsedTime(date)
        }
    }

    return dateRangeList.reversed().map {
        it to usageMap[it.dayOfWeek.value]!!
    }
}

@JvmName("calcDayUsedFromLaunch")
internal fun calcDayUsedList(
    dateRangeList: List<LocalDate>,
    list: Map<String, Int>
): List<Pair<LocalDate, Int>> {
    val usageMap = object : LinkedHashMap<Int, Int>() {
        init {
            put(7, 0)
            for (i in 1..6) {
                put(i, 0)
            }
        }
    }

    list.forEach {
        val date = LocalDate.parse(it.key, DateTimeFormatter.ISO_LOCAL_DATE)
        usageMap.computeIfPresent(date.dayOfWeek.value) { key, value ->
            value + it.value
        }
    }

    return dateRangeList.reversed().map {
        it to usageMap[it.dayOfWeek.value]!!
    }
}

fun chsLog(value: String) {
    Log.e("CHS_456", value)
}

fun LocalDate.toConvertDisplayYearDate(): String {
    return if (this == LocalDate.now()) {
        "오늘"
    } else {
        if (this.year == LocalDate.now().year) {
            return this.format(Constants.DATE_NAME_FORMAT)
        }
        this.format(Constants.YEAR_DATE_FORMAT_NAME)
    }
}

fun List<LocalDate>.toDisplayYearDate(): String {
    return if (this.min().year == this.max().year) {
        if (this.min().monthValue == this.max().monthValue) {
            "${this.min().format(Constants.DATE_FORMAT)}~${this.max().dayOfMonth}일"
        } else {
            "${this.min().format(Constants.DATE_FORMAT)} ~ ${
                this.max().format(Constants.DATE_FORMAT)
            }"
        }
    } else {
        "${this.min().format(Constants.YEAR_DATE_FORMAT)} ~ ${
            this.max().format(Constants.YEAR_DATE_FORMAT)
        }"
    }
}

fun LocalDate.containsWeek(targetDate: LocalDate): Boolean {
    return this.reverseDateUntilWeek(targetDate).count() == 7
}

fun LocalDate.reverseDateUntilWeek(targetDate: LocalDate): List<LocalDate> {
    return this.run {
        if (this.dayOfWeek == DayOfWeek.SUNDAY) return@run this
        this.minusDays(this.dayOfWeek.value.toLong())
    }.reverseDateUntil(
        targetDate.run {
            if (this.dayOfWeek == DayOfWeek.SUNDAY) {
                this.plusDays(6)
            } else {
                this.plusDays((6 - this.dayOfWeek.value).toLong())
            }
        }
    )
}

fun LocalDate.getYearOfWeek(): Int {
    return this.get(WeekFields.of(Locale.KOREA).weekOfYear())
}

fun LocalDate.toConvertDisplayDay(): String {
    return this.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREA)
}

fun List<Pair<LocalDate, Int>>.toCalcDailyUsage(): String {
    return this.sumOf { it.second }.run {
        if (this@toCalcDailyUsage.count { it.second != 0 } == 0) return@run 0

        this / this@toCalcDailyUsage.count { it.second != 0 }
    }.convertToRealUsageHour()
}