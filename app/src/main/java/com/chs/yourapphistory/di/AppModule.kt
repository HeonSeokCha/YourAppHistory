package com.chs.yourapphistory.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.work.WorkManager
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.YourAppHistoryDatabase
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.dao.UsageStateEventDao
import com.chs.yourapphistory.data.DataStoreSource
import com.chs.yourapphistory.data.db.dao.InCompleteAppUsageDao
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AppModule {

    @Single
    fun provideAppUtilSource(context: Context): ApplicationInfoSource {
        return ApplicationInfoSource(context)
    }

    @Single
    fun provideDatabase(context: Context): YourAppHistoryDatabase {
        return YourAppHistoryDatabase.getInstance(context)
    }

    @Single
    fun provideDataStorePref(context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(Constants.PREF_NAME)
        }
    }

    @Single
    fun provideWorkManager(context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Factory
    fun provideDataStoreSource(dataStorePref: DataStore<Preferences>): DataStoreSource {
        return DataStoreSource(dataStorePref)
    }


    @Factory
    fun provideAppUsageDao(db: YourAppHistoryDatabase): AppUsageDao {
        return db.appUsageDao
    }

    @Factory
    fun provideAppInfoDao(db: YourAppHistoryDatabase): AppInfoDao {
        return db.appInfoDao
    }

    @Factory
    fun provideAppForegroundUsageDao(db: YourAppHistoryDatabase): AppForegroundUsageDao {
        return db.appForegroundUsageDao
    }

    @Factory
    fun provideAppNotifyInfoDao(db: YourAppHistoryDatabase): AppNotifyInfoDao {
        return db.appNotifyInfoDao
    }

    @Factory
    fun provideUsageStateDao(db: YourAppHistoryDatabase): UsageStateEventDao {
        return db.usageStateEventDao
    }

    @Factory
    fun provideIncompleteInfoDao(db: YourAppHistoryDatabase): InCompleteAppUsageDao {
        return db.inCompleteAppUsageDao
    }

}