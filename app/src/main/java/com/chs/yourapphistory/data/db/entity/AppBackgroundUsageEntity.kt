package com.chs.yourapphistory.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "appBackgroundUsage",
    primaryKeys = ["packageName", "beginUseTime"]
)
data class AppBackgroundUsageEntity(
    val packageName: String,
    val beginUseTime: Long,
    val endUseTime: Long = 0L
)