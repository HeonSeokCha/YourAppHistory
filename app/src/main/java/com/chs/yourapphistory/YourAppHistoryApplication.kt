package com.chs.yourapphistory

import android.app.Application
import com.chs.yourapphistory.di.DataModule
import com.chs.yourapphistory.di.DomainModule
import com.chs.yourapphistory.di.PresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module


class YourAppHistoryApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@YourAppHistoryApplication)
            modules(
                DataModule().module,
                DomainModule().module,
                PresentationModule().module
            )
        }
    }
}