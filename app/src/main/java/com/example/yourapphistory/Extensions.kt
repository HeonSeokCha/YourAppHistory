package com.example.yourapphistory

import java.time.LocalDateTime
import java.time.ZoneId

fun Long.convertToRealUsageTime(): String {
    return this.toString()
}


fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()): Long {
    return atZone(zone)?.toInstant()?.toEpochMilli() ?: 0L
}