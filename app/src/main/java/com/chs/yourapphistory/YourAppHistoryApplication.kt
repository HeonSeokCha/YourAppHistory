package com.chs.yourapphistory

import android.app.Application
import com.chs.yourapphistory.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.data.workmanager.AppWorker
import org.koin.android.ext.android.inject
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.ksp.generated.defaultModule
import java.util.concurrent.TimeUnit

class YourAppHistoryApplication : Application(), Configuration.Provider {

    private val koinWorkerFactory: KoinWorkerFactory by inject()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(koinWorkerFactory)
            .build()

    private val workManager: WorkManager by inject()
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

            scheduleWorker()
        }
    }

    private fun scheduleWorker() {
        val request = PeriodicWorkRequestBuilder<AppWorker>(6, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.TAG_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}