package com.chs.yourapphistory.data.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chs.yourapphistory.domain.repository.AppRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltWorker
class AppWorker @AssistedInject constructor(
    private val repository: AppRepository,
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        repository.insertInstallAppInfo()
        repository.insertAppUsageInfo()

        return Result.success()
    }
}