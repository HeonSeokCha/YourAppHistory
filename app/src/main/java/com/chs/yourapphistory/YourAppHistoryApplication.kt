package com.chs.yourapphistory

import android.app.Application
import com.chs.yourapphistory.di.YourAppHistoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.plugin.module.dsl.startKoin


class YourAppHistoryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<YourAppHistoryModule>() {
            androidLogger()
            androidContext(this@YourAppHistoryApplication)
        }
    }
}