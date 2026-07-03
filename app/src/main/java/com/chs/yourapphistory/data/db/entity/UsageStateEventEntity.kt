package com.chs.yourapphistory.data.db.entity

import androidx.room3.Entity
import androidx.room3.Index

@Entity(
    tableName = "usageStateEvent",
    primaryKeys = ["packageName", "eventTime", "eventType"],
    indices = [
        Index(
            value = ["eventTime"],
            orders = [Index.Order.DESC]
        )
    ]
)
data class UsageStateEventEntity(
    val packageName: String,
    val className: String?,
    val eventTime: Long,
    val eventType: Int,
    val createTime: Long = System.currentTimeMillis()
)
