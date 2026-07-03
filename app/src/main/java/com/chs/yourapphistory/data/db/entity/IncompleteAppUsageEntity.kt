package com.chs.yourapphistory.data.db.entity

import androidx.room3.Entity
import androidx.room3.Index

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
    val className: String?,
    val createTime: Long
)
