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
          "LEFT JOIN appForegroundUsage ON (beginUseTime BETWEEN :targetDate AND :targetDate + 86399999 " +
            "OR endUseTime BETWEEN :targetDate AND :targetDate + 86399999) " +
           "AND appInfo.packageName = appForegroundUsage.packageName "
    )
    abstract suspend fun getDayForegroundUsedList(
        targetDate: Long
    ): Map<AppInfoEntity, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>

    @Query(
        "SELECT date(beginUseTime / 1000, 'unixepoch', 'localtime') as beginDate, beginUseTime, endUseTime " +
          "FROM appForegroundUsage " +
         "WHERE (beginUseTime BETWEEN :beginDate AND :endDate + 86399999 " +
            "OR endUseTime BETWEEN :beginDate AND :endDate + 86399999) " +
           "AND packageName = :packageName"
    )
    abstract suspend fun getWeeklyForegroundUsedList(
        beginDate: Long,
        endDate: Long,
        packageName: String
    ): Map<@MapColumn("beginDate") String, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>

    @Query(
        "SELECT beginUseTime, endUseTime " +
          "FROM appForegroundUsage " +
         "WHERE (beginUseTime BETWEEN :targetDate AND :targetDate + 86399999 " +
            "OR endUseTime BETWEEN :targetDate AND :targetDate + 86399999) " +
           "AND packageName = :packageName "
    )
    abstract suspend fun getForegroundUsageInfo(
        targetDate: Long,
        packageName: String
    ): Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>


    @Query("DELETE FROM appForegroundUsage")
    abstract suspend fun deleteAll()
}