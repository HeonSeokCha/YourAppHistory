package com.chs.yourapphistory.domain.model

data class AppInfo(
    override val packageName: String,
    val label: String,
) : BaseAppInfo
