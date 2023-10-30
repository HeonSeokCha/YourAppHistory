package com.example.yourapphistory.domain.model

import java.time.LocalDateTime

data class AppUsageInfo(
    val packageName: String,
    val beginUseTime: LocalDateTime,
    val endUseTime: LocalDateTime
)
