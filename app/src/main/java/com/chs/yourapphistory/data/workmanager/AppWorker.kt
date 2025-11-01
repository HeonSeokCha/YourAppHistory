package com.chs.yourapphistory.data.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chs.yourapphistory.common.getUsagePermission
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import org.koin.android.annotation.KoinWorker

@KoinWorker
class AppWorker(
    private val context: Context,
    parameters: WorkerParameters,
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