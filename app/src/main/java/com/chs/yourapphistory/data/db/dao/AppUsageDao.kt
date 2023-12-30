package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppUsageDao : BaseDao<AppUsageEntity> {

    @Query(
        "SELECT IFNULL(MIN(beginUseTime), 0) " +
          "FROM appUsage"
    )
    abstract suspend fun getOldestCollectTime(): Long

    @Query(
        "SELECT IFNULL(MAX(endUseTime), 0)" +
          "FROM appUsage "
    )
    abstract suspend fun getLastEndUseTime(): Long

    @Query(
        "SELECT date(:targetDate / 1000, 'unixepoch', 'localtime'), * " +
          "FROM appUsage " +
         "WHERE date(beginUseTime / 1000, 'unixepoch', 'localtime') " +
                "= date(:targetDate / 1000, 'unixepoch', 'localtime') "
    )
    abstract suspend fun getPagingDayUsageInfo(targetDate: Long):List<AppUsageEntity>

    @Query(
        "SELECT * " +
          "FROM appUsage " +
         "WHERE date(beginUseTime / 1000, 'unixepoch', 'localtime') " +
                "= date(:beginTime / 1000, 'unixepoch', 'localtime') " +
           "AND packageName = :packageName"
    )
    abstract suspend fun getUsageInfoList(
        beginTime: Long,
        packageName: String
    ): List<AppUsageEntity>
}