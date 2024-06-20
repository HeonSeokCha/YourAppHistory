package com.chs.yourapphistory.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.data.workmanager.AppWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val workManager: WorkManager
) : ViewModel() {

    fun executeWorker() {
        val request = PeriodicWorkRequestBuilder<AppWorker>(6, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.TAG_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}