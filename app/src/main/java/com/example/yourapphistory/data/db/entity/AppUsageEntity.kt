package com.example.yourapphistory.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "appUsage",
    primaryKeys = ["packageName", "beginTime"]
)
data class AppUsageEntity(
    val packageName: String,
    val beginTime: Long,
    val endTime: Long
)
