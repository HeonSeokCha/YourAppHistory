package com.example.yourapphistory.common

import java.text.SimpleDateFormat
import java.util.Locale


fun Long.toSimpleDateConvert(skipHour: Boolean = false): String {
    return if (skipHour) {
        SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(this)
    } else {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.KOREA).format(this)
    }
}

