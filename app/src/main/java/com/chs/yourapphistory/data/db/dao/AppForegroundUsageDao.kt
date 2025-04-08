package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity
import com.chs.yourapphistory.data.db.entity.AppInfoEntity

@Dao
abstract class AppForegroundUsageDao : BaseDao<AppForegroundUsageEntity> {

    @Query("SELECT IFNULL(MAX(endUseTime), 0) FROM appForegroundUsage")
    abstract suspend fun getLastTime(): Long

    @Query(
        "SELECT appInfo.*, appForegroundUsage.beginUseTime as beginUseTime, appForegroundUsage.endUseTime as endUseTime " +
          "FROM appInfo " +
          "LEFT JOIN appForegroundUsage ON (date(:targetDate / 1000, 'unixepoch', 'localtime') BETWEEN " +
            "date(beginUseTime / 1000, 'unixepoch', 'localtime') AND date(endUseTime / 1000, 'unixepoch', 'localtime')) " +
           "AND appInfo.packageName = appForegroundUsage.packageName "
    )
    abstract suspend fun getDayForegroundUsedList(
        targetDate: Long
    ): Map<AppInfoEntity, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>

    @Query(
        "SELECT beginUseTime, endUseTime " +
          "FROM appForegroundUsage " +
         "WHERE (date(:targetDate / 1000, 'unixepoch', 'localtime') BETWEEN " +
                "date(beginUseTime / 1000, 'unixepoch', 'localtime') AND date(endUseTime / 1000, 'unixepoch', 'localtime')) " +
           "AND packageName = :packageName "
    )
    abstract suspend fun getForegroundUsageInfo(
        targetDate: Long,
        packageName: String
    ): Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>


    @Query("DELETE FROM appForegroundUsage")
    abstract suspend fun deleteAll()
}