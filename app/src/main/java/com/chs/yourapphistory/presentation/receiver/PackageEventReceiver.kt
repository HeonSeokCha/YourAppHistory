package com.chs.yourapphistory.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.data.workmanager.AppRemoveWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject

class PackageEventReceiver : BroadcastReceiver(), KoinComponent {

    private val workManager: WorkManager by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        val data = intent.data?.schemeSpecificPart ?: return
        chsLog("onReceive : $data")

        val uploadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<AppRemoveWorker>()
                .setInputData(
                    workDataOf(Constants.DATA_KEY_REMOVE_PACKAGE_NAME to data)
                )
                .build()

        workManager.enqueue(uploadWorkRequest)
    }
}