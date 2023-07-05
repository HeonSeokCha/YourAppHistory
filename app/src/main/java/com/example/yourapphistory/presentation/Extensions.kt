package com.example.yourapphistory.presentation

import java.time.LocalDateTime
import java.time.ZoneId

fun Long.convertToRealUsageTime(): String {
    val hour: Long =  (this / 1000) / 60 / 60 % 24
    val minutes: Long = (this / 1000) / 60 % 60
    val second: Long = (this / 1000) % 60
    var result: String = ""
    if (hour != 0L) {
        result += "${hour}시간 "
    }

    if (minutes != 0L) {
        result += "${minutes}분 "
    }
    result += "${second}초"
    return result
}


fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()): Long {
    return atZone(zone)?.toInstant()?.toEpochMilli() ?: 0L
}