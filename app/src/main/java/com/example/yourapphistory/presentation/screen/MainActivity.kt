package com.example.yourapphistory.presentation.screen

import android.app.AppOpsManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.yourapphistory.presentation.screen.theme.YourAppHistoryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isUsagePermissionGranted by remember { mutableStateOf(checkPermission()) }
            YourAppHistoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isUsagePermissionGranted) {
                        MainScreen()
                    } else {
                        startActivity(
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                this.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                this.data = Uri.parse("package:${packageName}")
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        val appOps: AppOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode: Int =
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )

        return mode == AppOpsManager.MODE_ALLOWED
    }
}
