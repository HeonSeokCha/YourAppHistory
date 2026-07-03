package com.chs.yourapphistory.data.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity("appInfo")
data class AppInfoEntity(
    @PrimaryKey
    val packageName: String,
    val label: String,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val installProvider: String?,
    val lastUsedTime: Long?,
    val lastForegroundUsedTime: Long?
)
