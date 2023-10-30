package com.example.yourapphistory.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.yourapphistory.data.db.dao.AppUsageDao
import com.example.yourapphistory.data.db.dao.AppUsageEventDao
import com.example.yourapphistory.data.db.entity.AppUsageEntity
import com.example.yourapphistory.data.db.entity.AppUsageEventEntity

@Database(
    entities = [
        AppUsageEntity::class,
        AppUsageEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class YourAppHistoryDatabase : RoomDatabase() {
    abstract val appUsageDao: AppUsageDao
    abstract val appUsageEventDao: AppUsageEventDao

    companion object {
        fun getInstance(context: Context): YourAppHistoryDatabase {
            return Room
                .databaseBuilder(
                    context = context,
                    klass = YourAppHistoryDatabase::class.java,
                    "your_app_history_db"
                )
                .build()
        }
    }
}