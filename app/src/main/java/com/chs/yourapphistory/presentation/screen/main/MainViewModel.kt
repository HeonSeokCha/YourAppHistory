package com.chs.yourapphistory.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.data.workmanager.AppWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val workManager: WorkManager
) : ViewModel() {

    val selectPackageLabel: MutableStateFlow<String?> = MutableStateFlow(null)

    fun changeSelectPackageName(label: String?) {
        selectPackageLabel.update { label }
    }

    fun executeWorker() {
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