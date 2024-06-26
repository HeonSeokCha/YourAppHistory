package com.chs.yourapphistory.data.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chs.yourapphistory.common.getUsagePermission
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AppWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted parameters: WorkerParameters,
    private val insertInstallAppInfoUseCase: InsertInstallAppInfoUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        if (getUsagePermission(context)) {
            insertInstallAppInfoUseCase()
            insertAppUsageInfoUseCase()
        }

        return Result.success()
    }
}