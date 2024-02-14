package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity

@Dao
abstract class AppNotifyInfoDao : BaseDao<AppNotifyInfoEntity> {

    @Query("SELECT IFNULL(MIN(notifyTime), 0) FROM appNotifyInfo")
    abstract suspend fun getFirstCollectTime(): Long

    @Query("SELECT IFNULL(MAX(notifyTime), 0) FROM appNotifyInfo")
    abstract suspend fun getLastEventTime(): Long

    @Query(
        "SELECT * " +
          "FROM appNotifyInfo " +
         "WHERE date(notifyTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
           "AND packageName = :packageName"
    )
    abstract suspend fun getDayNotifyCount(
        packageName: String,
        targetDate: Long
    ): List<AppNotifyInfoEntity>
}