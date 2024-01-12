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

    @Query("DELETE FROM appUsage WHERE packageName = :packageName")
    abstract suspend fun deleteUsageInfo(packageName: String)

    @Query(
        "SELECT * " +
          "FROM appUsage " +
         "WHERE (date(beginUseTime / 1000, 'unixepoch', 'localtime') BETWEEN date(:beginDate / 1000, 'unixepoch', 'localtime') AND date(:endDate / 1000, 'unixepoch', 'localtime')" +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') BETWEEN date(:beginDate / 1000, 'unixepoch', 'localtime') AND date(:endDate / 1000, 'unixepoch', 'localtime'))" +
           "AND packageName = :packageName "
    )
    abstract suspend fun getDayUsageInfoList(
        beginDate: Long,
        endDate: Long,
        packageName: String
    ): List<AppUsageEntity>
}