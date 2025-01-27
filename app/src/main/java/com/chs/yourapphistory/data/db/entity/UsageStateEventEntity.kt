package com.chs.yourapphistory.data.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "usageStateEvent",
    primaryKeys = ["packageName", "eventTime"],
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
    val eventType: Int
)
