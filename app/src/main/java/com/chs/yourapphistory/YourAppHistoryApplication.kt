package com.chs.yourapphistory

import android.app.Application
import com.chs.yourapphistory.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module


class YourAppHistoryApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@YourAppHistoryApplication)
            modules(
                AppModule().module,
                defaultModule
            )
        }
    }
}