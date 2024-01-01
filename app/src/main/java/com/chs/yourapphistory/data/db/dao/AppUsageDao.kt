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
        "SELECT date(beginUseTime / 1000, 'unixepoch', 'localtime') as targetDate, * " +
          "FROM appUsage " +
         "WHERE (date(beginUseTime / 1000, 'unixepoch', 'localtime') BETWEEN date(:beginDate / 1000, 'unixepoch', 'localtime') AND date(:endDate / 1000, 'unixepoch', 'localtime')" +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') BETWEEN date(:beginDate / 1000, 'unixepoch', 'localtime') AND date(:endDate / 1000, 'unixepoch', 'localtime'))" +
           "AND packageName = :packageName " +
         "ORDER BY targetDate DESC"
    )
    abstract suspend fun getUsageInfoList(
        beginDate: Long,
        endDate: Long,
        packageName: String
    ): Map<@MapColumn("targetDate") String, List<AppUsageEntity>>
}