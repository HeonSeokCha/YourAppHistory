package com.chs.yourapphistory.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import com.chs.yourapphistory.data.db.entity.IncompleteAppUsageEntity

@Dao
abstract class InCompleteAppUsageDao : BaseDao<IncompleteAppUsageEntity> {
    @Query("SELECT IFNULL(MIN(beginUseTime), 0) FROM incompleteappusage")
    abstract suspend fun getMinBeginTime(): Long

    @Query("SELECT IFNULL(MIN(beginUseTime), 0) FROM incompleteappusage WHERE usageType = :type")
    abstract suspend fun getMinBeginTimeFromType(type: String): Long

    @Query("DELETE FROM inCompleteAppUsage")
    abstract suspend fun deleteAll()
}