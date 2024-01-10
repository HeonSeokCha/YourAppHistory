package com.chs.yourapphistory.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "appNotifyInfo",
    primaryKeys = ["packageName", "notifyTime"]
)
data class AppNotifyInfoEntity(
    val packageName: String,
    val notifyTime: Long
)