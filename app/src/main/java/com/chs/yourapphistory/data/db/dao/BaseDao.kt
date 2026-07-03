package com.chs.yourapphistory.data.db.dao

import androidx.room3.Delete
import androidx.room3.Upsert

interface BaseDao <T> {
    @Upsert
    suspend fun upsert(vararg entity: T)

    @Delete
    suspend fun delete(vararg entity: T)
}