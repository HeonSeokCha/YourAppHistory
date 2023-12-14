package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppInfoDao : BaseDao<AppInfoEntity> {

    @Query("SELECT packageName FROM appInfo")
    abstract suspend fun getAllPackageNames(): List<String>

    @Query(
        "DELETE " +
          "FROM appInfo " +
         "WHERE packageName = :packageName"
    )
    abstract suspend fun deleteAppInfo(packageName: String)

    @Query(
        "SELECT * " +
          "FROM appInfo " +
         "INNER JOIN appUsage ON (appUsage.beginUseTime BETWEEN :beginTime AND :endTime " +
            "OR appUsage.endUseTime BETWEEN :beginTime AND :endTime)" +
           "AND appUsage.packageName = appInfo.packageName "
    )
    abstract fun getDayUsedAppInfoList(
        beginTime: Long,
        endTime: Long
    ): Flow<Map<AppInfoEntity, List<AppUsageEntity>>>
}