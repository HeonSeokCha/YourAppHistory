package com.example.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.yourapphistory.data.db.entity.AppUsageEventEntity

@Dao
abstract class AppUsageEventDao : BaseDao<AppUsageEventEntity> {
    @Query(
        "DELETE FROM appUsageEvent " +
         "WHERE eventTime >= :time"
    )
    abstract suspend fun deleteTempEvent(time: Long)
}