package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity

@Dao
abstract class AppForegroundUsageDao : BaseDao<AppForegroundUsageEntity> {

    @Query(
        "SELECT IFNULL(MAX(endUseTime), 0) " +
          "FROM appForegroundUsage "
    )
    abstract suspend fun getLastEventTime(): Long

    @Query(
        "SELECT * " +
          "FROM appForegroundUsage " +
         "WHERE date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
           "AND date(:targetDate / 1000, 'unixepoch', 'localtime') " +
           "AND packageName = :packageName "
    )
    abstract suspend fun getDayForegroundUsageInfo(
        targetDate: Long,
        packageName: String
    ): List<AppForegroundUsageEntity>
}