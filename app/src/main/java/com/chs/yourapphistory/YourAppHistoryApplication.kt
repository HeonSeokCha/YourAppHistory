package com.chs.yourapphistory

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class YourAppHistoryApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workFactory)
            .build()
}