package com.chs.yourapphistory.common

import java.time.format.DateTimeFormatter

object Constants {
    const val FIRST_COLLECT_DAY: Long = 6L
    val SIMPLE_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    val SIMPLE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val SIMPLE_HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("a h시")
    val SIMPLE_HOUR_FORMAT_SIMPLE: DateTimeFormatter = DateTimeFormatter.ofPattern("h시")
    val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM월 dd일 (E)")

}