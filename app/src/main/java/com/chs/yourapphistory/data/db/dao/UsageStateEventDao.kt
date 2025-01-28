package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.UsageStateEventEntity

@Dao
abstract class UsageStateEventDao : BaseDao<UsageStateEventEntity> {

    @Query("SELECT IFNULL(MIN(eventTime), 0) FROM usageStateEvent")
    abstract suspend fun getFirstEventTime(): Long

    @Query("SELECT IFNULL(MAX(eventTime), 0) FROM  usageStateEvent")
    abstract suspend fun getLastEventTime(): Long
}