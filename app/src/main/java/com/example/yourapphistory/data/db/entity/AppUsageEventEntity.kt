package com.example.yourapphistory.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "appUsageEvent",
    primaryKeys = ["packageName", "eventTime", "eventType"]
)
data class AppUsageEventEntity(
    val packageName: String,
    val eventTime: Long,
    val eventType: Int,
    val className: String?
)
