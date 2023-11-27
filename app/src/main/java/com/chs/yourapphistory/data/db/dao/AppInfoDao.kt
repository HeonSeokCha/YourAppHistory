package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity

@Dao
abstract class AppInfoDao : BaseDao<AppInfoEntity> {

    @Query(
        "SELECT appInfo.*, " +
               "(appUsage.endUseTime - appUsage.beginUseTime) AS time " +
          "FROM appInfo " +
         "INNER JOIN appUsage ON appUsage.beginUseTime BETWEEN :beginTime AND :endTime " +
           "AND appUsage.packageName = appInfo.packageName " +
         "ORDER BY time DESC"
    )
    abstract suspend fun getDayUsedAppInfoList(
        beginTime: Long,
        endTime: Long
    ): Map<AppInfoEntity, @MapColumn("time")Long>

    @Query("SELECT packageName FROM appInfo")
    abstract suspend fun getAllPackageNames(): List<String>
}