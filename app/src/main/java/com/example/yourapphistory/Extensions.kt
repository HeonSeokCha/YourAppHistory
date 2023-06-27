package com.example.yourapphistory

import java.time.LocalDateTime
import java.time.ZoneId

fun Long.convertToRealUsageTime(): String {
    val hour: Long =  (this / 1000) / 60 / 60 % 24
    val minutes: Long = (this / 1000) / 60 % 60
    val second: Long = (this / 1000) % 60
    return "${hour}시간 ${minutes}분 ${second}초"
}


fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()): Long {
    return atZone(zone)?.toInstant()?.toEpochMilli() ?: 0L
}