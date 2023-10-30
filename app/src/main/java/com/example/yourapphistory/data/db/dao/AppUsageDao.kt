package com.example.yourapphistory.data.db.dao

import androidx.room.Dao
import com.example.yourapphistory.data.db.entity.AppUsageEntity

@Dao
abstract class AppUsageDao : BaseDao<AppUsageEntity> {
}