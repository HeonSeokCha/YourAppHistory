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
        "SELECT appInfo.*, appUsage.beginUseTime as beginUseTime, appUsage.endUseTime as endUseTime " +
          "FROM appInfo " +
          "LEFT JOIN appUsage ON (date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime')) " +
           "AND appInfo.packageName = appUsage.packageName "
    )
    abstract suspend fun getDayUsedAppInfoList(
        targetDate: Long
    ): Map<AppInfoEntity, Map<@MapColumn("beginUseTime") Long, @MapColumn("endUseTime") Long>>
}