package com.chs.yourapphistory.data.db.dao

import androidx.room.Upsert

interface BaseDao <T> {
    @Upsert
    suspend fun insert(vararg entity: T)
}