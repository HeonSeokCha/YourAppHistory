package com.chs.yourapphistory.data.db.dao

import androidx.room.Delete
import androidx.room.Upsert

interface BaseDao <T> {
    @Upsert
    suspend fun insert(vararg entity: T)

    @Delete
    suspend fun delete(vararg entity: T)
}