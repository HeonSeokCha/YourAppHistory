package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppInfoDao : BaseDao<AppInfoEntity> {

    @Query("SELECT * FROM appInfo")
    abstract suspend fun getAllPackage(): List<AppInfoEntity>

    @Query(
        "SELECT appInfo.*, appUsage.beginUseTime as beginUseTime, appUsage.endUseTime as endUseTime " +
          "FROM appInfo " +
          "LEFT JOIN appUsage ON (date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime')) " +
           "AND appInfo.packageName = appUsage.packageName "
    )
    abstract suspend fun getDayUsedList(
        targetDate: Long
    ): Map<AppInfoEntity, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>


    @Query(
        "SELECT appInfo.*, appForegroundUsage.beginUseTime as beginUseTime, appForegroundUsage.endUseTime as endUseTime " +
          "FROM appInfo " +
          "LEFT JOIN appForegroundUsage ON (date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime')) " +
           "AND appInfo.packageName = appForegroundUsage.packageName "
    )
    abstract suspend fun getDayForegroundUsedList(
        targetDate: Long
    ): Map<AppInfoEntity, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>


    @Query(
        "SELECT appInfo.*, COUNT(appNotifyInfo.packageName) as cnt " +
          "FROM appInfo " +
          "LEFT JOIN appNotifyInfo ON date(notifyTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
           "AND appInfo.packageName = appNotifyInfo.packageName " +
         "GROUP BY appInfo.packageName " +
         "ORDER BY cnt DESC, appInfo.packageName ASC "
    )
    abstract suspend fun getDayNotifyList(
        targetDate: Long
    ): Map<AppInfoEntity, @MapColumn("cnt") Int>

    @Query(
        "SELECT appInfo.* , COUNT(appUsage.packageName) as cnt " +
          "FROM appInfo " +
          "LEFT JOIN appUsage ON date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
           "AND appInfo.packageName = appUsage.packageName " +
         "GROUP BY appInfo.packageName " +
         "ORDER BY cnt DESC, appInfo.packageName ASC "
    )
    abstract suspend fun getDayLaunchList(
        targetDate: Long
    ): Map<AppInfoEntity, @MapColumn("cnt") Int>

}