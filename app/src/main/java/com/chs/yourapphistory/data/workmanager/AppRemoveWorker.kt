package com.chs.yourapphistory.data.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.domain.usecase.DeleteUsageInfoUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AppRemoveWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val parameters: WorkerParameters,
    private val deleteUsageInfoUseCase: DeleteUsageInfoUseCase
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        parameters.inputData.getString(Constants.DATA_KEY_REMOVE_PACKAGE_NAME).run {
            if (this.isNullOrEmpty()) return@run

            deleteUsageInfoUseCase(this)
        }

        return Result.success()
    }
}