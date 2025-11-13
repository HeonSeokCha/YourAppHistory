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

class PackageEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        val data = intent.data?.schemeSpecificPart ?: return

        val uploadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<AppRemoveWorker>()
                .setInputData(
                    workDataOf(Constants.DATA_KEY_REMOVE_PACKAGE_NAME to data)
                )
                .build()

        WorkManager.getInstance(context).enqueue(uploadWorkRequest)
    }
}