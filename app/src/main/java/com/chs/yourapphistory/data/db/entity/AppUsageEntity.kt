package com.chs.yourapphistory.data.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "appUsage",
    primaryKeys = ["packageName", "beginUseTime"],
    indices = [
        Index(
            value = ["beginUseTime", "endUseTime"],
            orders = [Index.Order.DESC, Index.Order.DESC]
        )
    ]
)
data class AppUsageEntity(
    val packageName: String,
    val beginUseTime: Long,
    val endUseTime: Long = 0L
)
