package com.example.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.yourapphistory.data.db.entity.AppUsageEntity

@Dao
abstract class AppUsageDao : BaseDao<AppUsageEntity> {

    @Query(
        "SELECT IFNULL(MAX(endUseTime), 0) " +
          "FROM appUsage "
    )
    abstract suspend fun getLastEndUseTime(): Long

    @Query(
        "SELECT * " +
          "FROM appUsage " +
         "WHERE beginUseTime >= :date"
    )
    abstract fun getDayUsageInfoList(date: Long)
}