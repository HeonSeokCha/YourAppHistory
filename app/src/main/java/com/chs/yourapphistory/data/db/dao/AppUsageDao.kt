package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppUsageDao : BaseDao<AppUsageEntity> {

    @Query("DELETE FROM appUsage WHERE packageName IN(:packageNames)")
    abstract suspend fun deleteFromPackageName(packageNames: List<String>)

    @Query(
        "SELECT appInfo.*, appUsage.beginUseTime as beginUseTime, appUsage.endUseTime as endUseTime " +
          "FROM appInfo " +
          "LEFT JOIN appUsage ON (date(:targetDate / 1000, 'unixepoch', 'localtime') BETWEEN " +
                    "date(beginUseTime / 1000, 'unixepoch', 'localtime') AND date(endUseTime / 1000, 'unixepoch', 'localtime')) " +
           "AND appInfo.packageName = appUsage.packageName "
    )
    abstract suspend fun getDayAppUsedInfo(
        targetDate: Long
    ): Map<AppInfoEntity, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>

    @Query(
        "SELECT appInfo.* , COUNT(appUsage.packageName) as cnt " +
          "FROM appInfo " +
          "LEFT JOIN appUsage ON beginUseTime BETWEEN :targetDate AND :targetDate + 86399999 " +
           "AND appInfo.packageName = appUsage.packageName " +
         "GROUP BY appInfo.packageName " +
         "ORDER BY cnt DESC, appInfo.label ASC"
    )
    abstract suspend fun getDayAppLaunchInfo(
        targetDate: Long
    ): Map<AppInfoEntity, @MapColumn("cnt") Int>

    @Query(
        "SELECT beginUseTime, endUseTime " +
          "FROM appUsage " +
         "WHERE (date(:targetDate / 1000, 'unixepoch', 'localtime') BETWEEN " +
               "date(beginUseTime / 1000, 'unixepoch', 'localtime') AND date(endUseTime / 1000, 'unixepoch', 'localtime')) " +
           "AND packageName = :packageName"
    )
    abstract suspend fun getDayPackageUsageInfo(
        targetDate: Long,
        packageName: String
    ): Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>

    @Query(
        "SELECT beginUseTime " +
          "FROM appUsage " +
         "WHERE beginUseTime BETWEEN :targetDate AND :targetDate + 86399999 " +
           "AND packageName = :packageName"
    )
    abstract suspend fun getDayPackageLaunchInfo(
        targetDate: Long,
        packageName: String
    ): List<Long>

    @Query(
        "SELECT date(beginUseTime / 1000, 'unixepoch', 'localtime') as beginDate, COUNT(beginUseTime) as cnt " +
          "FROM appUsage " +
         "WHERE beginUseTime BETWEEN :beginDate AND :endDate " +
           "AND packageName = :packageName " +
         "GROUP BY date(beginUseTime / 1000, 'unixepoch', 'localtime')"
    )
    abstract suspend fun getWeeklyAppLaunchInfo(
        beginDate: Long,
        endDate: Long,
        packageName: String
    ): Map<@MapColumn("beginDate") String, @MapColumn("cnt") Int>

//    @Query(
//        "SELECT date(beginUseTime / 1000, 'unixepoch', 'localtime') as beginDate, beginUseTime, endUseTime " +
//          "FROM appUsage " +
//         "WHERE (beginUseTime >= :beginDate AND endUseTime <= :endDate) " +
//           "AND packageName = :packageName"
//    )
//    abstract suspend fun getWeeklyAppUsageInfo(
//        beginDate: Long,
//        endDate: Long,
//        packageName: String
//    ): Map<@MapColumn("beginDate") String, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>

    @Query("DELETE FROM appUsage")
    abstract suspend fun deleteAll()

    @Query("SELECT IFNULL(MAX(endUseTime), 0) FROM appUsage")
    abstract suspend fun getLastTime(): Long
}