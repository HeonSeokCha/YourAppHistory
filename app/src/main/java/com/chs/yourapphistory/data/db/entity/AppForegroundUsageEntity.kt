package com.chs.yourapphistory.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "appForegroundUsage",
    primaryKeys = ["packageName", "beginUseTime"]
)
data class AppForegroundUsageEntity(
    val packageName: String,
    val beginUseTime: Long,
    val endUseTime: Long = 0L
)