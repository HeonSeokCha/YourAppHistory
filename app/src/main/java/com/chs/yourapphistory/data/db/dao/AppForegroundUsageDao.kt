package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity
import com.chs.yourapphistory.data.db.entity.AppInfoEntity

@Dao
abstract class AppForegroundUsageDao : BaseDao<AppForegroundUsageEntity> {

    @Query(
        "SELECT IFNULL(MIN(beginUseTime), 0) " +
          "FROM appForegroundUsage"
    )
    abstract suspend fun getFirstCollectTime(): Long

    @Query(
        "SELECT IFNULL(MAX(endUseTime), 0) " +
          "FROM appForegroundUsage "
    )
    abstract suspend fun getLastEventTime(): Long

    @Query(
        "SELECT appInfo.*, appForegroundUsage.beginUseTime as beginUseTime, appForegroundUsage.endUseTime as endUseTime " +
          "FROM appForegroundUsage " +
          "LEFT JOIN appInfo ON appInfo.packageName = appForegroundUsage.packageName " +
         "WHERE (date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime')) " +
           "AND appInfo.packageName = appForegroundUsage.packageName " +
         "ORDER BY (endUseTime - beginUseTime) DESC, appInfo.label ASC"
    )
    abstract suspend fun getDayForegroundUsedList(
        targetDate: Long
    ): Map<AppInfoEntity, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>

    @Query(
        "SELECT beginUseTime, endUseTime " +
          "FROM appForegroundUsage " +
         "WHERE (date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime')) " +
           "AND packageName = :packageName "
    )
    abstract suspend fun getForegroundUsageInfo(
        targetDate: Long,
        packageName: String
    ): Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>
}