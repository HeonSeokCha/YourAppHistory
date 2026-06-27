package com.chs.yourapphistory.presentation.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.getUsagePermission
import com.chs.yourapphistory.presentation.worker.AppWorker
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val workManager: WorkManager by inject()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        executeWorker()

        setContent {
            val backstack = rememberNavBackStack().apply {
                this.clear()
                if (getUsagePermission(this@MainActivity)) this.add(MainScreens.ScreenTotalSummary)
//                if (getUsagePermission(this@MainActivity)) this.add(MainScreens.ScreenUsedAppList(SortType.UsageEvent))
                else this.add(MainScreens.ScreenWelcome)
            }

            Scaffold(
                topBar = {
                    MainAppbar(
                        screen = backstack.last(),
                        onBack = { backstack.removeLastOrNull() },
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
            ) {
                NavigationRoot(
                    modifier = Modifier.padding(it),
                    backStack = backstack
                )
            }
        }
    }

    private fun executeWorker() {
        val request = PeriodicWorkRequestBuilder<AppWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.TAG_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}