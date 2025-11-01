package com.chs.yourapphistory

import android.app.Application
import com.chs.yourapphistory.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import androidx.work.Configuration
import org.koin.android.ext.android.inject
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.ksp.generated.defaultModule

class YourAppHistoryApplication : Application(), Configuration.Provider {

    private val koinWorkerFactory: KoinWorkerFactory by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@YourAppHistoryApplication)
            modules(
                AppModule().module,
                defaultModule
            )
            workManagerFactory()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(koinWorkerFactory)
            .build()
}