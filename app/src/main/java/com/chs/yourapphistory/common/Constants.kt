package com.chs.yourapphistory.common

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.activity.ComponentActivity
import java.time.format.DateTimeFormatter

object Constants {
    const val FIRST_COLLECT_DAY: Long = 6L
    val SIMPLE_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    val SIMPLE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun getUsagePermission(context: Context): Boolean {
        val appOps: AppOpsManager = context.getSystemService(ComponentActivity.APP_OPS_SERVICE) as AppOpsManager
        return try {
            val mode: Int =
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
}