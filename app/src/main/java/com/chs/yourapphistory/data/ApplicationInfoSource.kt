package com.chs.yourapphistory.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.isZero
import com.chs.yourapphistory.common.toLocalDateTime
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.model.AppUsageEventRawInfo
import com.chs.yourapphistory.data.model.UsageEventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun getApplicationIcon(packageName: String): Bitmap? {
        return withContext(Dispatchers.IO) {
           try {
                context.packageManager.getApplicationIcon(packageName).toBitmap(
                    width = 144, height = 144
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    fun getUsageEvent(
        usageType: UsageEventType,
        beginTime: Long
    ): List<AppUsageEventRawInfo> {
        val usageEvents: UsageEvents =
            (context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager).run {
                queryEvents(beginTime, System.currentTimeMillis())
            }

        val resultArr: ArrayList<AppUsageEventRawInfo> = arrayListOf()
        val usageFilterList = when (usageType) {
            is UsageEventType.AppUsageEvent -> {
                Constants.APP_USAGE_EVENT_FILTER
            }
            is UsageEventType.AppNotifyEvent -> {
                listOf(12)
            }
            is UsageEventType.AppForegroundUsageEvent -> {
                Constants.APP_FOREGROUND_USAGE_EVENT_FILTER
            }
        }

        while (usageEvents.hasNextEvent()) {
            val currentEvent = UsageEvents.Event().apply {
                usageEvents.getNextEvent(this)
            }

            val packageName: String = currentEvent.packageName
            val time: Long = currentEvent.timeStamp
            val eventType: Int = currentEvent.eventType

            if (usageFilterList.any { it == eventType }) {
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

//        resultArr.map {
//            Log.e("RAW", "${it.packageName} : ${it.eventTime.toLocalDateTime().format(Constants.SIMPLE_DATE_FORMAT)} | ${it.eventType}")
//        }
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
               && installPackageNames.any { packageName -> packageName == it.packageName}
       }.forEach {
           if (it.eventType == UsageEvents.Event.FOREGROUND_SERVICE_START) {
               if (!inCompletedUsageList.containsKey(it.packageName))
                inCompletedUsageList[it.packageName] = AppForegroundUsageEntity(
                    packageName = it.packageName,
                    beginUseTime = it.eventTime
                )
           } else {
               if (inCompletedUsageList.containsKey(it.packageName)) {
                   completedUsageList.add(inCompletedUsageList[it.packageName]!!.copy(
                       endUseTime = it.eventTime
                   ))

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
