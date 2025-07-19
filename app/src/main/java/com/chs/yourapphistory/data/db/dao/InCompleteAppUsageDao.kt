package com.chs.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.chs.yourapphistory.data.db.entity.IncompleteAppUsageEntity

@Dao
abstract class InCompleteAppUsageDao : BaseDao<IncompleteAppUsageEntity> {
    @Query("SELECT IFNULL(MIN(beginUseTime), 0) FROM incompleteappusage")
    abstract suspend fun getMinBeginTime(): Long

    @Query("SELECT * FROM incompleteappusage WHERE usageType = :type")
    abstract suspend fun getListFromType(type: String): List<IncompleteAppUsageEntity>

    @Query("DELETE FROM inCompleteAppUsage")
    abstract suspend fun deleteAll()
}