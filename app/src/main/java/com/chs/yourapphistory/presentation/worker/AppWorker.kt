package com.chs.yourapphistory.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chs.yourapphistory.common.getUsagePermission
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinWorker

@KoinWorker
class AppWorker(
    private val context: Context,
    parameters: WorkerParameters,
    private val insertInstallAppInfoUseCase: InsertInstallAppInfoUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        if (!getUsagePermission(context)) return Result.success()

        withContext(Dispatchers.IO) { insertInstallAppInfoUseCase() }
        insertAppUsageInfoUseCase()

        return Result.success()
    }
}
