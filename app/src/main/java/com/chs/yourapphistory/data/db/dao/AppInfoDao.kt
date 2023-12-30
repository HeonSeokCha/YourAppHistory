package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppInfoDao : BaseDao<AppInfoEntity> {

    @Query("SELECT * FROM appInfo")
    abstract suspend fun getAllPackage(): List<AppInfoEntity>

    @Query(
        "DELETE " +
          "FROM appInfo " +
         "WHERE packageName = :packageName"
    )
    abstract suspend fun deleteAppInfo(packageName: String)

    @Query(
        "SELECT date(beginUseTime / 1000, 'unixepoch', 'localtime') as targetDate, * " +
          "FROM appInfo " +
         "INNER JOIN appUsage ON (date(beginUseTime / 1000, 'unixepoch', 'localtime') BETWEEN " +
        "date(:beginDate / 1000, 'unixepoch', 'localtime') AND date(:endDate / 1000, 'unixepoch', 'localtime')" +
        "OR date(endUseTime / 1000, 'unixepoch', 'localtime') BETWEEN " +
        "date(:beginDate / 1000, 'unixepoch', 'localtime') AND date(:endDate / 1000, 'unixepoch', 'localtime'))" +
           "AND appUsage.packageName = appInfo.packageName "
    )
    abstract suspend fun getDayUsedAppInfoList(
        beginDate: Long,
        endDate: Long
    ): Map<@MapColumn("targetDate") String, Map<AppInfoEntity, List<AppUsageEntity>>>
}