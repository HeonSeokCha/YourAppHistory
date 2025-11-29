package com.chs.yourapphistory.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.data.workmanager.AppWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel
import java.util.concurrent.TimeUnit

@KoinViewModel
class MainViewModel (
    private val workManager: WorkManager
) : ViewModel() {

    init {
        executeWorker()
    }

    private fun executeWorker() {
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