package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppUsageDao : BaseDao<AppUsageEntity> {

    @Query(
        "SELECT IFNULL(MIN(beginUseTime), 0) " +
          "FROM appUsage"
    )
    abstract suspend fun getFirstCollectTime(): Long

    @Query(
        "SELECT IFNULL(MAX(endUseTime), 0)" +
                "FROM appUsage "
    )
    abstract suspend fun getLastEventTime(): Long

    @Query("DELETE FROM appUsage WHERE packageName IN(:packageNames)")
    abstract suspend fun deleteUsageInfo(packageNames: List<String>)


    @Query(
        "SELECT appInfo.*, appUsage.beginUseTime as beginUseTime, appUsage.endUseTime as endUseTime " +
          "FROM appUsage " +
          "LEFT JOIN appInfo ON appInfo.packageName = appUsage.packageName " +
         "WHERE (date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime')) " +
         "ORDER BY (endUseTime - beginUseTime) DESC, appInfo.label ASC"
    )
    abstract suspend fun getDayUsedList(
        targetDate: Long
    ): Map<AppInfoEntity, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>

    @Query(
        "SELECT beginUseTime, endUseTime " +
          "FROM appUsage " +
         "WHERE (date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime')) " +
           "AND packageName = :packageName"
    )
    abstract suspend fun getDayUsageInfoList(
        targetDate: Long,
        packageName: String
    ): Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>

    @Query(
        "SELECT beginUseTime " +
          "FROM appUsage " +
         "WHERE date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
           "AND packageName = :packageName"
    )
    abstract suspend fun getDayUsageBeginInfoList(
        targetDate: Long,
        packageName: String
    ): List<Long>

    @Query("DELETE FROM appUsage")
    abstract suspend fun deleteAllUsageInfo()
}