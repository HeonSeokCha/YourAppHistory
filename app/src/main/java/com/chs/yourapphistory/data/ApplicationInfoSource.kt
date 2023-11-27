package com.chs.yourapphistory.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import com.chs.yourapphistory.common.isZero
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.db.model.AppUsageEventRawInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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
                PackageManager.GET_META_DATA
            )
        }.map {
            it.activityInfo.packageName
        }
    }

    fun getApplicationLabel(packageName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    fun getApplicationIcon(packageName: String): Bitmap? {
        return try {
            context.packageManager.getApplicationIcon(packageName).toBitmap()
        } catch (e: PackageManager.NameNotFoundException) {
            null
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
            val eventType: Int = currentEvent.eventType

            if (eventType == UsageEvents.Event.ACTIVITY_RESUMED
                || eventType == UsageEvents.Event.ACTIVITY_PAUSED
                || eventType == UsageEvents.Event.ACTIVITY_STOPPED
                || eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE
                || eventType == UsageEvents.Event.SCREEN_INTERACTIVE
                || eventType == UsageEvents.Event.FOREGROUND_SERVICE_START
                || eventType == UsageEvents.Event.FOREGROUND_SERVICE_STOP
            ) {
                resultArr.add(
                    AppUsageEventRawInfo(
                        packageName = packageName,
                        className = currentEvent.className,
                        eventType = eventType,
                        eventTime = time
                    )
                )
            }
        }
        return resultArr
    }

    fun getAppUsageInfoList(usageEventList: List<AppUsageEventRawInfo>): List<AppUsageEntity> {
        val installPackageNames: List<String> = getInstalledLauncherPackageNameList()
        var prevPackageName: String? = null
        var prevActivityClassName: String? = null
        val inCompletedUsageList: HashMap<String, AppUsageEntity> = hashMapOf()
        var isScreenOff: Boolean = false
        val completedUsageList: ArrayList<AppUsageEntity> = arrayListOf()

        for (usageEvent in usageEventList) {
            when (usageEvent.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (inCompletedUsageList[usageEvent.packageName] == null) {
                        inCompletedUsageList[usageEvent.packageName] =
                            AppUsageEntity(
                                packageName = usageEvent.packageName,
                                beginUseTime = usageEvent.eventTime
                            )
                    }
                    prevPackageName = usageEvent.packageName
                    prevActivityClassName = usageEvent.className
                }

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    inCompletedUsageList.computeIfPresent(
                        usageEvent.packageName
                    ) { key, value ->
                        value.copy(
                            endUseTime = usageEvent.eventTime
                        )
                    }

                    if (isScreenOff) {
                        if (installPackageNames.contains(usageEvent.packageName)) {
                            if (inCompletedUsageList.containsKey(usageEvent.packageName)) {
                                if (inCompletedUsageList[usageEvent.packageName]!!.endUseTime.isZero()) {
                                    inCompletedUsageList[usageEvent.packageName] =
                                        inCompletedUsageList[usageEvent.packageName]!!.copy(
                                            endUseTime = usageEvent.eventTime
                                        )
                                }
                                completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!)
                            }
                        }

                        inCompletedUsageList.remove(usageEvent.packageName)
                    }
                }

                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (inCompletedUsageList.containsKey(usageEvent.packageName)
                        && (prevPackageName != usageEvent.packageName || prevActivityClassName == usageEvent.className)
                    ) {
                        if (installPackageNames.contains(usageEvent.packageName)) {
                            if (inCompletedUsageList[usageEvent.packageName]!!.endUseTime.isZero()) {
                                inCompletedUsageList[usageEvent.packageName] =
                                    inCompletedUsageList[usageEvent.packageName]!!.copy(
                                        endUseTime = usageEvent.eventTime
                                    )
                            }

                            completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!)
                        }

                        inCompletedUsageList.remove(usageEvent.packageName)
                    }
                }

                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    prevPackageName = usageEvent.packageName
                    isScreenOff = true
                }

                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    isScreenOff = false
                }

//                UsageEvents.Event.FOREGROUND_SERVICE_START, UsageEvents.Event.FOREGROUND_SERVICE_STOP -> {
//                    Log.e("USAGE", usageEvent.toString())
//                }
            }
        }
        return completedUsageList
    }
}
