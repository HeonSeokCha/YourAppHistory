package com.chs.yourapphistory.data.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chs.yourapphistory.domain.repository.AppRepository
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltWorker
class AppWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val insertInstallAppInfoUseCase: InsertInstallAppInfoUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        insertInstallAppInfoUseCase()
        insertAppUsageInfoUseCase()
        return Result.success()
    }
}