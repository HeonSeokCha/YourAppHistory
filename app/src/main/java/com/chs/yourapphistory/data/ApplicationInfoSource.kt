package com.chs.yourapphistory.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.isZero
import com.chs.yourapphistory.common.toLocalDateTime
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.model.AppUsageEventRawInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

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

//        resultArr.forEach {
//            chsLog("${it.packageName} | ${it.eventTime.toLocalDateTime()} - ${it.eventType} - ${it.className}")
//        }

        return resultArr
    }

    fun getAppForeGroundUsageInfoList(
        installPackageNames: List<String>,
        usageEventList: List<AppUsageEventRawInfo>
    ): List<AppForegroundUsageEntity> {
        val inCompletedUsageList: HashMap<Pair<String, String?>, AppForegroundUsageEntity> =
            hashMapOf()
        val completedUsageList: ArrayList<AppForegroundUsageEntity> = arrayListOf()

        usageEventList.forEach {
//            chsLog("${it.packageName} | ${it.eventTime.toLocalDateTime()} - ${it.eventType} - ${it.className}")

            when (it.eventType) {
                UsageEvents.Event.FOREGROUND_SERVICE_START -> {
                    if (!installPackageNames.contains(it.packageName)) return@forEach

                    inCompletedUsageList[it.packageName to it.className] = AppForegroundUsageEntity(
                        packageName = it.packageName,
                        beginUseTime = it.eventTime
                    )
                }

                UsageEvents.Event.FOREGROUND_SERVICE_STOP -> {
                    if (inCompletedUsageList.containsKey(it.packageName to it.className)) {
                        completedUsageList.add(
                            inCompletedUsageList[it.packageName to it.className]!!.copy(
                                endUseTime = it.eventTime
                            )
                        )

                        inCompletedUsageList.remove(it.packageName to it.className)
                    }
                }

                UsageEvents.Event.DEVICE_SHUTDOWN -> {
                    inCompletedUsageList.replaceAll { key, value ->
                        value.copy(
                            endUseTime = it.eventTime
                        )
                    }

                    completedUsageList.addAll(inCompletedUsageList.map { it.value })
                    inCompletedUsageList.clear()
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
        val inCompletedUsageList: HashMap<String, Pair<AppUsageEntity, ArrayList<String?>>> =
            hashMapOf()
        var isScreenOff: Boolean = false
        val completedUsageList: ArrayList<AppUsageEntity> = arrayListOf()

        for (usageEvent in usageEventList) {
//            chsLog("${usageEvent.packageName} | ${usageEvent.eventTime.toLocalDateTime()} - ${usageEvent.eventType} - ${usageEvent.className}")

            when (usageEvent.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (installPackageNames.contains(usageEvent.packageName)) {
                        if (inCompletedUsageList[usageEvent.packageName] == null) {
                            inCompletedUsageList[usageEvent.packageName] =
                                AppUsageEntity(
                                    packageName = usageEvent.packageName,
                                    beginUseTime = usageEvent.eventTime
                                ) to arrayListOf(usageEvent.className)
                        } else {
                            inCompletedUsageList.computeIfPresent(usageEvent.packageName) { _, value ->
                                value.copy(
                                    second = value.second.apply {
                                        if (usageEvent.packageName == prevPackageName) {
                                            this.add(usageEvent.className)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    prevPackageName = usageEvent.packageName
                    prevClassName = usageEvent.className
                }

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (inCompletedUsageList[usageEvent.packageName] == null) continue

                    if (prevPackageName != null && usageEvent.packageName != prevPackageName) {
                        if (inCompletedUsageList[prevPackageName] != null
                            && inCompletedUsageList[prevPackageName]!!.first.endUseTime != 0L
                        ) {
                            completedUsageList.add(inCompletedUsageList[prevPackageName]!!.first)
                            inCompletedUsageList.remove(prevPackageName)
                        }
                    }

                    inCompletedUsageList.computeIfPresent(usageEvent.packageName) { _, value ->
                        value.copy(value.first.copy(endUseTime = usageEvent.eventTime))
                    }

                    if (isScreenOff) {
                        completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!.first)
                        inCompletedUsageList.remove(usageEvent.packageName)
                        continue
                    }
                }

                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (inCompletedUsageList[usageEvent.packageName] == null) continue

                    if (usageEvent.packageName != prevPackageName) {
                        inCompletedUsageList.filter {
                            it.key != usageEvent.packageName
                                    && it.key != prevPackageName
                                    && it.value.first.endUseTime != 0L
                        }.forEach {
                            completedUsageList.add(it.value.first)
                            inCompletedUsageList.remove(it.key)
                        }
                    }

                    inCompletedUsageList.computeIfPresent(usageEvent.packageName) { _, value ->
                        value.copy(
                            second = value.second.apply {
                                this.remove(usageEvent.className)
                            }
                        )
                    }

                    if (isScreenOff) {
                        if (usageEvent.packageName == prevPackageName
                            && usageEvent.className != prevClassName
                        ) {
                            continue
                        }

                        completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!.first)
                        inCompletedUsageList.remove(usageEvent.packageName)
                        continue
                    }

                    if (inCompletedUsageList[usageEvent.packageName]!!.second.isEmpty()) {
                        completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!.first)
                        inCompletedUsageList.remove(usageEvent.packageName)
                    } else {
                        if (usageEvent.packageName != prevPackageName
                            && usageEvent.className != prevClassName
                        ) {
                            completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!.first)
                            inCompletedUsageList.remove(usageEvent.packageName)
                        }
                    }
                }

                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    isScreenOff = true
                    inCompletedUsageList.filter {
                        it.value.first.endUseTime != 0L
                    }.forEach {
                        completedUsageList.add(it.value.first)
                        inCompletedUsageList.remove(it.key)
                    }
                }

                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    isScreenOff = false
                }

                UsageEvents.Event.DEVICE_SHUTDOWN -> {
                    inCompletedUsageList.replaceAll { key, value ->
                        value.copy(
                            value.first.copy(
                                endUseTime = usageEvent.eventTime
                            )
                        )
                    }

                    completedUsageList.addAll(inCompletedUsageList.map { it.value.first })
                    inCompletedUsageList.clear()

                    prevPackageName = null
                    prevClassName = null
                    isScreenOff = false
                }
            }
        }

//        completedUsageList.map {
//            Log.e(
//                "CHS_123",
//                "${it.packageName} | ${it.beginUseTime.toLocalDateTime()} - ${it.endUseTime.toLocalDateTime()} ${
//                    (it.endUseTime - it.beginUseTime).toInt().convertToRealUsageTime()
//                }"
//            )
//        }
        return completedUsageList
    }
}
