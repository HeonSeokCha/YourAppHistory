package com.example.yourapphistory.data

import android.content.Context
import com.example.yourapphistory.data.db.YourAppHistoryDatabase
import com.example.yourapphistory.data.db.dao.AppUsageDao
import com.example.yourapphistory.data.db.dao.AppUsageEventDao
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
    fun provideAppUsageEventDao(db: YourAppHistoryDatabase): AppUsageEventDao {
        return db.appUsageEventDao
    }

}