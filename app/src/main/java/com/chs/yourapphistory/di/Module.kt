package com.chs.yourapphistory.di

import android.content.Context
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.YourAppHistoryDatabase
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
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
    fun provideAppUsageDao(db: YourAppHistoryDatabase): AppUsageDao {
        return db.appUsageDao
    }

    @Singleton
    @Provides
    fun provideAppInfoDao(db: YourAppHistoryDatabase): AppInfoDao {
        return db.appInfoDao
    }

    @Singleton
    @Provides
    fun provideAppForegroundUsageDao(db: YourAppHistoryDatabase): AppForegroundUsageDao {
        return db.appForegroundUsageDao
    }


    @Singleton
    @Provides
    fun provideAppNotifyInfoDao(db: YourAppHistoryDatabase): AppNotifyInfoDao {
        return db.appNotifyInfoDao
    }
}