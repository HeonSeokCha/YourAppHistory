package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.model.AppInfoData
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppInfoDao : BaseDao<AppInfoEntity> {

    @Query("DELETE FROM appInfo WHERE packageName IN(:packageNames)")
    abstract suspend fun deleteFromPackageName(packageNames: List<String>)


    @Query("""
        SELECT packageName,
               label
          FROM appInfo
         ORDER BY label
    """)
    abstract suspend fun getPackageInfo(): List<AppInfoData>


    @Query("SELECT * FROM appInfo ORDER BY label")
    abstract suspend fun getPackageDetailInfo(): List<AppInfoEntity>

    @Query("SELECT * FROM appInfo ORDER BY lastUsedTime, lastForegroundUsedTime, firstInstallTime DESC")
    abstract suspend fun getSortLastUsedPackageDetailInfo(): List<AppInfoEntity>

    @Query("SELECT * FROM appInfo ORDER BY label")
    abstract suspend fun getAllInstallPackageDetailInfo(): List<AppInfoEntity>
}