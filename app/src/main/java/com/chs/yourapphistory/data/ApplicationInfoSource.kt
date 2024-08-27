package com.chs.yourapphistory.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.compose.ui.util.packInts
import androidx.core.graphics.drawable.toBitmap
import androidx.room.util.copy
import com.chs.yourapphistory.common.atEndOfDayToMillis
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.isZero
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toLocalDateTime
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.model.AppUsageEventRawInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import kotlin.contracts.contract

class ApplicationInfoSource @Inject constructor(
    private val context: Context
) {

    fun getInstalledLauncherPackageNameList(): List<String> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            this.addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            context.packageManager.queryIntentActivities(
                mainIntent,
                0
            )
        }.map {
            it.activityInfo.packageName
        }
    }

    suspend fun getApplicationLabel(packageName: String): String {
        return withContext(Dispatchers.Default) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.ApplicationInfoFlags.of(0)
                    )
                )
            } else {
                context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(
                        packageName,
                        0
                    )
                )
            }.toString()
        }
    }

    suspend fun getApplicationIconMap(installPackageNames: List<String>): HashMap<String, Bitmap?> {
        return withContext(Dispatchers.IO) {
            installPackageNames.associateWithTo(HashMap()) {
                try {
                    context.packageManager.getApplicationIcon(it).toBitmap(
                        width = 144, height = 144
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }
        }
    }

    fun getUsageEvent(beginTime: Long): List<AppUsageEventRawInfo> {
        val usageEvents: UsageEvents =
            (context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager).run {
                queryEvents(beginTime, System.currentTimeMillis())
//                queryEvents(
//                    LocalDate.now().minusDays(4L).atStartOfDayToMillis(),
//                    LocalDate.now().minusDays(4L).atEndOfDayToMillis()
//                )
            }

        val resultArr: ArrayList<AppUsageEventRawInfo> = arrayListOf()

        while (usageEvents.hasNextEvent()) {
            val currentEvent = UsageEvents.Event().apply {
                usageEvents.getNextEvent(this)
            }

            val packageName: String = currentEvent.packageName
            val time: Long = currentEvent.timeStamp
            val className: String? = currentEvent.className
            val eventType: Int = currentEvent.eventType

            resultArr.add(
                AppUsageEventRawInfo(
                    packageName = packageName,
                    className = className,
                    eventType = eventType,
                    eventTime = time
                )
            )
        }

        return resultArr
    }

    fun getAppForeGroundUsageInfoList(
        installPackageNames: List<String>,
        usageEventList: List<AppUsageEventRawInfo>
    ): List<AppForegroundUsageEntity> {
        val inCompletedUsageList: HashMap<String, AppForegroundUsageEntity> = hashMapOf()
        val completedUsageList: ArrayList<AppForegroundUsageEntity> = arrayListOf()

        usageEventList.filter {
            (it.eventType == UsageEvents.Event.FOREGROUND_SERVICE_START
                    || it.eventType == UsageEvents.Event.FOREGROUND_SERVICE_STOP)
                    && installPackageNames.any { packageName -> packageName == it.packageName }
        }.forEach {
            if (it.eventType == UsageEvents.Event.FOREGROUND_SERVICE_START) {
                if (!inCompletedUsageList.containsKey(it.packageName))
                    inCompletedUsageList[it.packageName] = AppForegroundUsageEntity(
                        packageName = it.packageName,
                        beginUseTime = it.eventTime
                    )
            } else {
                if (inCompletedUsageList.containsKey(it.packageName)) {
                    completedUsageList.add(
                        inCompletedUsageList[it.packageName]!!.copy(
                            endUseTime = it.eventTime
                        )
                    )

                    inCompletedUsageList.remove(it.packageName)
                }
            }
        }
        return completedUsageList
    }

    fun getAppNotifyInfoList(
        installPackageNames: List<String>,
        usageEventList: List<AppUsageEventRawInfo>
    ): List<AppNotifyInfoEntity> {
        return usageEventList.filter {
            it.eventType == 12
                    && installPackageNames.any { packageName -> packageName == it.packageName }
        }.map {
            AppNotifyInfoEntity(
                packageName = it.packageName,
                notifyTime = it.eventTime
            )
        }
    }

    fun getAppUsageInfoList(
        installPackageNames: List<String>,
        usageEventList: List<AppUsageEventRawInfo>
    ): List<AppUsageEntity> {
        var prevPackageName: String? = null
        var prevClassName: String? = null
        val inCompletedUsageList: HashMap<String, Pair<AppUsageEntity, Int>> = hashMapOf()
        var isScreenOff: Boolean = false
        val completedUsageList: ArrayList<AppUsageEntity> = arrayListOf()

        for (usageEvent in usageEventList) {
            chsLog("${usageEvent.packageName} | ${usageEvent.eventTime.toLocalDateTime()} - ${usageEvent.className} - ${usageEvent.eventType}")

            when (usageEvent.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (installPackageNames.contains(usageEvent.packageName)) {
                        if (inCompletedUsageList[usageEvent.packageName] == null) {
                            inCompletedUsageList[usageEvent.packageName] =
                                AppUsageEntity(
                                    packageName = usageEvent.packageName,
                                    beginUseTime = usageEvent.eventTime
                                ) to 1
                        } else {
                            inCompletedUsageList.computeIfPresent(usageEvent.packageName) { _, value ->
                                value.copy(
                                    second = value.second + 1
                                )
                            }
                        }
                    }

                    prevPackageName = usageEvent.packageName
                    prevClassName = usageEvent.className
                }

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (prevPackageName != null && usageEvent.packageName != prevPackageName) {
                        if (inCompletedUsageList[prevPackageName] != null
                            && inCompletedUsageList[prevPackageName]!!.first.endUseTime != 0L
                        ) {
                            completedUsageList.add(inCompletedUsageList[prevPackageName]!!.first)
                            inCompletedUsageList.remove(prevPackageName)
                        }
                    }

                    if (inCompletedUsageList[usageEvent.packageName] == null) continue

                    inCompletedUsageList.computeIfPresent(usageEvent.packageName) { _, value ->
                        value.copy(value.first.copy(endUseTime = usageEvent.eventTime))
                    }
                }

                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (inCompletedUsageList[usageEvent.packageName] == null) continue

                    inCompletedUsageList.computeIfPresent(usageEvent.packageName) { _, value ->
                        value.copy(
                            first = if (value.first.endUseTime.isZero()) {
                                value.first.copy(endUseTime = usageEvent.eventTime)
                            } else value.first,
                            second = value.second - 1
                        )
                    }

                    if (inCompletedUsageList[usageEvent.packageName]!!.second <= 0
                        || usageEvent.packageName != prevPackageName
                        || isScreenOff
                    ) {

                        if (isScreenOff) {
                            if (inCompletedUsageList[usageEvent.packageName]!!.second > 0) {
                                completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!.first)
                                inCompletedUsageList.remove(usageEvent.packageName)
                                continue
                            }

                            if (inCompletedUsageList[usageEvent.packageName]!!.second <= 0
                                && usageEvent.packageName == prevPackageName
                            ) {
                                completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!.first)
                                inCompletedUsageList.remove(usageEvent.packageName)
                                continue
                            }

                            if (inCompletedUsageList[usageEvent.packageName]!!.second <= 0
                                && usageEvent.packageName != prevPackageName
                            ) {
                                completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!.first)
                                inCompletedUsageList.remove(usageEvent.packageName)
                                continue
                            }

                            continue
                        }

                        if (inCompletedUsageList[usageEvent.packageName]!!.second <= 0
                            && usageEvent.packageName == prevPackageName
                            && usageEvent.className != prevClassName
                        ) {
                            continue
                        }

                        completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!.first)
                        inCompletedUsageList.remove(usageEvent.packageName)
                    }
                }

                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    isScreenOff = true
                }

                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    isScreenOff = false
                }
            }
        }
        completedUsageList.map {
            Log.e(
                "CHS_123",
                "${it.packageName} | ${it.beginUseTime.toLocalDateTime()} - ${it.endUseTime.toLocalDateTime()} ${
                    (it.endUseTime - it.beginUseTime).toInt().convertToRealUsageMinutes()
                }"
            )
        }
        return completedUsageList
    }
}
