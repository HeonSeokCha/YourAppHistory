package com.example.yourapphistory.data.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.example.yourapphistory.data.db.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppUsageDao : BaseDao<AppUsageEntity> {

    @Query(
        "SELECT IFNULL(MIN(beginUseTime), 0) " +
          "FROM appUsage"
    )
    abstract suspend fun getOldestCollectTime(): Long

    @Query(
        "SELECT *" +
          "FROM appUsage " +
         "ORDER BY beginUseTime DESC LIMIT 1"
    )
    abstract suspend fun getLastEndUseTime(): AppUsageEntity?

    @Query(
        "SELECT A.packageName, B.* " +
          "FROM appUsage AS A " +
         "INNER JOIN appUsage AS B " +
            "ON A.packageName = B.packageName " +
           "AND A.beginUseTime = B.beginUseTime " +
           "AND A.endUseTime = B.endUseTime " +
         "WHERE A.beginUseTime BETWEEN :date AND (:date + 86399999) " +
         "ORDER BY A.packageName, B.beginUseTime ASC"
    )
    abstract suspend fun getDayUsageInfoList(date: Long): Map<@MapColumn("packageName")String, List<AppUsageEntity>>
}