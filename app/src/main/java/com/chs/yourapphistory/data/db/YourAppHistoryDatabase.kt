package com.chs.yourapphistory.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.dao.InCompleteAppUsageDao
import com.chs.yourapphistory.data.db.dao.UsageStateEventDao
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.db.entity.IncompleteAppUsageEntity
import com.chs.yourapphistory.data.db.entity.UsageStateEventEntity

@Database(
    entities = [
        AppInfoEntity::class,
        AppUsageEntity::class,
        AppForegroundUsageEntity::class,
        AppNotifyInfoEntity::class,
        UsageStateEventEntity::class,
        IncompleteAppUsageEntity::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
abstract class YourAppHistoryDatabase : RoomDatabase() {
    abstract val appUsageDao: AppUsageDao
    abstract val appInfoDao: AppInfoDao
    abstract val appForegroundUsageDao: AppForegroundUsageDao
    abstract val appNotifyInfoDao: AppNotifyInfoDao
    abstract val usageStateEventDao: UsageStateEventDao
    abstract val inCompleteAppUsageDao: InCompleteAppUsageDao

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