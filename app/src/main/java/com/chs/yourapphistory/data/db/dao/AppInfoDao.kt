package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppInfoDao : BaseDao<AppInfoEntity> {

    @Query("SELECT packageName FROM appInfo")
    abstract suspend fun getAllPackageNames(): List<String>

    @Query(
        "SELECT appInfo.*, " +
               "SUM((appUsage.endUseTime - appUsage.beginUseTime)) AS time " +
          "FROM appInfo " +
         "INNER JOIN appUsage ON appUsage.beginUseTime BETWEEN :beginTime AND :endTime " +
           "AND appUsage.packageName = appInfo.packageName " +
         "GROUP BY appInfo.packageName " +
         "ORDER BY time DESC"
    )
    abstract fun getDayUsedAppInfoList(
        beginTime: Long,
        endTime: Long
    ): Flow<Map<AppInfoEntity, @MapColumn("time") Long>>
}