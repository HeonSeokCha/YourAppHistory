package com.example.yourapphistory.presentation.screen

import android.app.AppOpsManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.yourapphistory.presentation.screen.theme.YourAppHistoryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var hasPermission by remember { mutableStateOf(false) }
            val usageStateLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (getUsagePermission()) {
                    Toast.makeText(this, "Usage OK", Toast.LENGTH_SHORT).show()
                    hasPermission = true
                } else {
                    Toast.makeText(this, "Usage NOT OK", Toast.LENGTH_SHORT).show()
                }
            }
            LaunchedEffect(Unit) {
                if (getUsagePermission()) {
                    hasPermission = true
                } else {
                    usageStateLauncher.launch(
                        Intent(
                            Settings.ACTION_USAGE_ACCESS_SETTINGS,
                            Uri.parse("package:${this@MainActivity.packageName}")
                        )
                    )
                }
            }

            if (hasPermission) {
                MainScreen()
            }
        }
    }


    private fun getUsagePermission(): Boolean {
        val appOps: AppOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        return try {
            val mode: Int =
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    packageName
                )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
}
