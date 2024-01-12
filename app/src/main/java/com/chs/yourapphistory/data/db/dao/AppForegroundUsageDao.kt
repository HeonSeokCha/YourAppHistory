package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity

@Dao
abstract class AppForegroundUsageDao : BaseDao<AppForegroundUsageEntity> {
    @Query(
        "SELECT * " +
         "FROM appForegroundUsage " +
         "WHERE (date(beginUseTime / 1000, 'unixepoch', 'localtime') BETWEEN date(:beginDate / 1000, 'unixepoch', 'localtime') AND date(:endDate / 1000, 'unixepoch', 'localtime')" +
            "OR date(endUseTime / 1000, 'unixepoch', 'localtime') BETWEEN date(:beginDate / 1000, 'unixepoch', 'localtime') AND date(:endDate / 1000, 'unixepoch', 'localtime'))" +
           "AND packageName = :packageName "
    )
    abstract suspend fun getDayForegroundUsageInfo(
        beginDate: Long,
        endDate: Long,
        packageName: String
    ): List<AppForegroundUsageEntity>
}