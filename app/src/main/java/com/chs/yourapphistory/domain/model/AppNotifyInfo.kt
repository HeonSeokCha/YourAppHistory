package com.chs.yourapphistory.domain.model

import java.time.LocalDateTime

data class AppNotifyInfo(
    val packageName: String,
    val notifyTime: LocalDateTime
)
