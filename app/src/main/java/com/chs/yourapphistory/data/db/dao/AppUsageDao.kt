package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
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
        "SELECT * " +
          "FROM appUsage " +
         "WHERE (beginUseTime BETWEEN :beginTime AND :endTime " +
           " OR endUseTime BETWEEN :beginTime AND :endTime) " +
           "AND packageName = :packageName"
    )
    abstract suspend fun getUsageInfoList(
        beginTime: Long,
        endTime: Long,
        packageName: String
    ): List<AppUsageEntity>
}