package com.chs.yourapphistory.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.domain.usecase.DeleteUsageInfoUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PackageEventReceiver : BroadcastReceiver(), KoinComponent {

    private val deleteUsageInfoUseCase: DeleteUsageInfoUseCase by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            if (intent == null || context == null) return@launch
            if (intent.action != Intent.ACTION_PACKAGE_FULLY_REMOVED) return@launch
            val data = intent.data?.schemeSpecificPart ?: return@launch

            try {
                chsLog("onReceive : $data")
                deleteUsageInfoUseCase(data)
            } finally {
                pendingResult.finish()
            }
        }
    }
}