package com.example.yourapphistory.presentation.screen

import android.app.AppOpsManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.example.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase
    @Inject lateinit var insertInstallAppInfoUseCase: InsertInstallAppInfoUseCase

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
                    lifecycleScope.launch {
                        insertAppUsageInfoUseCase()
                        insertAppUsageInfoUseCase()
                    }
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

    override fun onRestart() {
        super.onRestart()
        lifecycleScope.launch(Dispatchers.IO) {
            insertAppUsageInfoUseCase()
            insertInstallAppInfoUseCase()
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
