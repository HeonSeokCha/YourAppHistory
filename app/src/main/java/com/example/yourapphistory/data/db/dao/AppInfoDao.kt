package com.example.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.yourapphistory.data.db.entity.AppInfoEntity

@Dao
abstract class AppInfoDao : BaseDao<AppInfoEntity> {

    @Query(
        "SELECT appInfo.packageName, " +
               "(appUsage.endUseTime - appUsage.beginUseTime) AS time " +
          "FROM appInfo " +
         "INNER JOIN appUsage " +
            "ON appUsage.beginUseTime BETWEEN :beginTime AND :endTime " +
           "AND appUsage.packageName = appInfo.packageName "
    )
    abstract fun getDayUsedAppInfoList(
        beginTime: Long,
        endTime: Long
    )
}