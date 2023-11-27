package com.chs.yourapphistory.common

import java.time.format.DateTimeFormatter

object Constants {
    const val FIRST_COLLECT_DAY: Long = 5L
    val SIMPLE_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    val SIMPLE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
}