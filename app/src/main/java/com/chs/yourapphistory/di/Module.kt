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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Singleton
    @Provides
    fun provideAppUtilSource(@ApplicationContext context: Context): ApplicationInfoSource {
        return ApplicationInfoSource(context)
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): YourAppHistoryDatabase {
        return YourAppHistoryDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideDataStorePref(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(Constants.PREF_NAME)
        }
    }

    @Singleton
    @Provides
    fun provideDataStoreSource(dataStorePref: DataStore<Preferences>): DataStoreSource {
        return DataStoreSource(dataStorePref)
    }

    @Provides
    fun provideAppUsageDao(db: YourAppHistoryDatabase): AppUsageDao {
        return db.appUsageDao
    }

    @Provides
    fun provideAppInfoDao(db: YourAppHistoryDatabase): AppInfoDao {
        return db.appInfoDao
    }

    @Provides
    fun provideAppForegroundUsageDao(db: YourAppHistoryDatabase): AppForegroundUsageDao {
        return db.appForegroundUsageDao
    }

    @Provides
    fun provideAppNotifyInfoDao(db: YourAppHistoryDatabase): AppNotifyInfoDao {
        return db.appNotifyInfoDao
    }

    @Provides
    fun provideUsageStateDao(db: YourAppHistoryDatabase): UsageStateEventDao {
        return db.usageStateEventDao
    }

    @Singleton
    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}