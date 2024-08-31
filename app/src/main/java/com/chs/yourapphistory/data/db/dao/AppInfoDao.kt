package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppInfoDao : BaseDao<AppInfoEntity> {

    @Query("SELECT * FROM appInfo")
    abstract suspend fun getAllPackage(): List<AppInfoEntity>

    @Query(
        "SELECT appInfo.* , COUNT(appUsage.packageName) as cnt " +
          "FROM appInfo " +
          "LEFT JOIN appUsage ON date(beginUseTime / 1000, 'unixepoch', 'localtime') = date(:targetDate / 1000, 'unixepoch', 'localtime') " +
           "AND appInfo.packageName = appUsage.packageName " +
         "GROUP BY appInfo.packageName " +
         "ORDER BY cnt DESC, appInfo.packageName ASC "
    )
    abstract suspend fun getDayLaunchList(
        targetDate: Long
    ): Map<AppInfoEntity, @MapColumn("cnt") Int>

}