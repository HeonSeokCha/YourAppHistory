package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity

@Dao
abstract class AppNotifyInfoDao : BaseDao<AppNotifyInfoEntity> {

    @Query("DELETE FROM appNotifyInfo WHERE packageName IN(:packageNames)")
    abstract suspend fun deleteFromPackageName(packageNames: List<String>)

    @Query(
        "SELECT appInfo.*, COUNT(appNotifyInfo.packageName) as cnt " +
          "FROM appInfo " +
          "LEFT JOIN appNotifyInfo ON notifyTime BETWEEN :targetDate AND :targetDate + 86399999 " +
           "AND appInfo.packageName = appNotifyInfo.packageName " +
         "GROUP BY appInfo.packageName "
    )
    abstract suspend fun getDayNotifyList(
        targetDate: Long
    ): Map<AppInfoEntity, @MapColumn("cnt") Int>

    @Query(
        "SELECT COUNT(packageName) " +
                 "FROM appNotifyInfo " +
                "WHERE notifyTime BETWEEN :targetDate AND :targetDate + 86399999"
    )
    abstract suspend fun getDayAppNotify(targetDate: Long): Long

    @Query(
        "SELECT notifyTime " +
                 "FROM appNotifyInfo " +
                "WHERE notifyTime BETWEEN :targetDate AND :targetDate + 86399999 " +
                  "AND packageName = :packageName"
    )
    abstract suspend fun getDayNotifyCount(
        packageName: String,
        targetDate: Long
    ): List<Long>

    @Query(
        "SELECT date(notifyTime / 1000, 'unixepoch', 'localtime') as beginDate, COUNT(notifyTime) as cnt " +
          "FROM appNotifyInfo " +
         "WHERE notifyTime BETWEEN :beginDate AND :endDate " +
           "AND packageName = :packageName " +
         "GROUP BY date(notifyTime / 1000, 'unixepoch', 'localtime')"
    )
    abstract suspend fun getWeeklyNotifyCount(
        beginDate: Long,
        endDate: Long,
        packageName: String,
    ): Map<@MapColumn("beginDate") String, @MapColumn("cnt") Int>

    @Query("SELECT IFNULL(MAX(notifyTime), 0) FROM appNotifyInfo")
    abstract suspend fun getLastTime(): Long

    @Query("DELETE FROM appNotifyInfo")
    abstract suspend fun deleteAll()
}