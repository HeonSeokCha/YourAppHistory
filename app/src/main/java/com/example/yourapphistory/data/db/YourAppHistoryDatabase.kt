package com.example.yourapphistory.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yourapphistory.data.db.dao.AppInfoDao
import com.example.yourapphistory.data.db.dao.AppUsageDao
import com.example.yourapphistory.data.db.entity.AppInfoEntity
import com.example.yourapphistory.data.db.entity.AppUsageEntity

@Database(
    entities = [
        AppInfoEntity::class,
        AppUsageEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(BitmapConverters::class)
abstract class YourAppHistoryDatabase : RoomDatabase() {
    abstract val appUsageDao: AppUsageDao
    abstract val appInfoDao: AppInfoDao

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