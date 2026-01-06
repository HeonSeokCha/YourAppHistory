package com.chs.yourapphistory.data.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Upsert

interface BaseDao <T> {
    @Upsert
    suspend fun upsert(vararg entity: T)

    @Delete
    suspend fun delete(vararg entity: T)
}