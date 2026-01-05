package com.chs.yourapphistory.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
