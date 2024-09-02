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

}