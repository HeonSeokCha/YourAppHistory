package com.chs.yourapphistory.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "appUsage",
    primaryKeys = ["packageName", "beginUseTime"]
)
data class AppUsageEntity(
    val packageName: String,
    val beginUseTime: Long,
    val endUseTime: Long = 0L
)
