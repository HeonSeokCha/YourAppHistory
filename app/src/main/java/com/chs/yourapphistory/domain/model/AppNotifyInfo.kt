package com.chs.yourapphistory.domain.model

import java.time.LocalDateTime

data class AppNotifyInfo(
    override val packageName: String,
    val notifyTime: LocalDateTime
) : BaseAppInfo
