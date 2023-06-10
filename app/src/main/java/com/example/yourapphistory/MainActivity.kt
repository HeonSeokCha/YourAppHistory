package com.example.yourapphistory

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.yourapphistory.ui.theme.YourAppHistoryTheme
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAppHistoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var usageInfoList: List<AppUsageInfo> by remember { mutableStateOf(listOf()) }
                    MainScreen(
                        usageList = usageInfoList,
                        onQueryUsage = { beginTime, endTime ->
                            getUsageList(
                                beginTime,
                                endTime
                            ) {
                                usageInfoList = it
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }


    private fun getUsageList(
        beginDate: Long,
        endDate: Long,
        onResult: (List<AppUsageInfo>) -> Unit
    ) {
        val usm: UsageStatsManager =
            this.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val launch_able_apps: List<ResolveInfo> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.packageManager.queryIntentActivities(
                    mainIntent, 0
                )

            } else {
                this.packageManager.queryIntentActivities(mainIntent, PackageManager.GET_META_DATA)
            }

        val usageEvents = usm.queryEvents(beginDate, endDate)
        val eventUsage: HashMap<String, AppUsageInfo> = hashMapOf()
        val totalUsage: ArrayList<AppUsageInfo> = arrayListOf()
        var prevEventPackageName: String? = null
        val unFinishedPackageList: ArrayList<String> = arrayListOf()

        while (usageEvents.hasNextEvent()) {
            val currentEvent = UsageEvents.Event().apply {
                usageEvents.getNextEvent(this)
            }

            val packageName: String = currentEvent.packageName
            val time: Long = currentEvent.timeStamp

            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED
            ) {

                if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {

                    if (prevEventPackageName != null
                        && prevEventPackageName != packageName
                        && eventUsage.containsKey(prevEventPackageName)
                    ) {
                        if (eventUsage.containsKey(packageName)
                            && unFinishedPackageList.any { it == packageName }
                        ) {
                            totalUsage.add(eventUsage[packageName]!!)
                            eventUsage.remove(packageName)
                            unFinishedPackageList.remove(packageName)
                        }

                        if (eventUsage[prevEventPackageName]?.endTime == 0L) {
                            if (launch_able_apps.any { it.activityInfo.packageName == prevEventPackageName }) {
                                unFinishedPackageList.add(prevEventPackageName)
                                eventUsage[prevEventPackageName] = eventUsage[prevEventPackageName]!!.copy(
                                    endTime = time
                                )
                            } else {
                                eventUsage.remove(prevEventPackageName)
                            }
                        } else {
                            if (launch_able_apps.any { it.activityInfo.packageName == prevEventPackageName }) {
                                if (isRealUsedPackage(eventUsage[prevEventPackageName]!!)) {
                                    totalUsage.add(eventUsage[prevEventPackageName]!!)
                                }
                            }
                            eventUsage.remove(prevEventPackageName)
                        }
                    }

                    if (eventUsage[packageName] == null) {
                        eventUsage[packageName] = AppUsageInfo(
                            packageName = packageName,
                            beginTime = time
                        )
                        prevEventPackageName = packageName
                    } else {
                        eventUsage[packageName] = eventUsage[packageName]!!.copy(endTime = 0L)
                    }

                } else {
                    if (eventUsage.containsKey(packageName)) {
                        eventUsage[packageName] = eventUsage[packageName]!!.copy(
                            endTime = time
                        )
                    }

                    if (unFinishedPackageList.any { it == packageName }) {
                        if (isRealUsedPackage(eventUsage[packageName]!!)) {
                            totalUsage.add(eventUsage[packageName]!!)
                            eventUsage.remove(packageName)
                        }
                        unFinishedPackageList.remove(packageName)
                    }
                }
            }
        }

        eventUsage.forEach { eventUsage ->  // 비정상 종료된 앱들
            if (launch_able_apps.any { it.activityInfo.packageName == eventUsage.key} ) {
                if (isRealUsedPackage(eventUsage.value)) {
                    totalUsage.add(eventUsage.value)
                }
            }
        }
        eventUsage.clear()
        unFinishedPackageList.clear()

        onResult(totalUsage.sortedBy { it.beginTime })

    }

    private fun isRealUsedPackage(packageUsageInfo: AppUsageInfo): Boolean {
        return (packageUsageInfo.endTime - packageUsageInfo.beginTime) > 1000L
                && packageUsageInfo.endTime != 0L
    }

    private fun checkPermission() {
        val appOps: AppOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode: Int =
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )

        if (mode != AppOpsManager.MODE_ALLOWED) {
            startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    this.data = Uri.parse("package:${packageName}")
                }
            )
        }
    }
}
