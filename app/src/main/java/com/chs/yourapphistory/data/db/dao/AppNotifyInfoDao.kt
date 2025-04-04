package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity

@Dao
abstract class AppNotifyInfoDao : BaseDao<AppNotifyInfoEntity> {

    @Query(
        "SELECT appInfo.*, COUNT(appNotifyInfo.packageName) as cnt " +
          "FROM appInfo " +
          "LEFT JOIN appNotifyInfo ON date(notifyTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
           "AND appInfo.packageName = appNotifyInfo.packageName " +
         "GROUP BY appInfo.packageName "
    )
    abstract suspend fun getDayNotifyList(
        targetDate: Long
    ): Map<AppInfoEntity, @MapColumn("cnt") Int>

    @Query(
        "SELECT notifyTime " +
          "FROM appNotifyInfo " +
         "WHERE date(notifyTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
           "AND packageName = :packageName"
    )
    abstract suspend fun getDayNotifyCount(
        packageName: String,
        targetDate: Long
    ): List<Long>

    @Query(
        "SELECT date(notifyTime / 1000, 'unixepoch', 'localtime') as beginDate, COUNT(notifyTime) as cnt " +
          "FROM appNotifyInfo " +
         "WHERE date(notifyTime / 1000, 'unixepoch', 'localtime') BETWEEN date(:beginDate / 1000, 'unixepoch', 'localtime') AND date(:endDate / 1000, 'unixepoch', 'localtime')" +
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