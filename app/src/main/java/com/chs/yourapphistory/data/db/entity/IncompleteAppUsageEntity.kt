package com.chs.yourapphistory.data.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "inCompleteAppUsage",
    primaryKeys = ["packageName", "usageType", "beginUseTime"],
    indices = [
        Index(value = ["beginUseTime"], orders = [Index.Order.ASC])
    ]
)
data class IncompleteAppUsageEntity(
    val packageName: String,
    val usageType: String,
    val beginUseTime: Long,
    val className: String?
)
