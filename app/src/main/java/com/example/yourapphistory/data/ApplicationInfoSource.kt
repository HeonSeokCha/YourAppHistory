package com.example.yourapphistory.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import com.example.yourapphistory.common.isZero
import com.example.yourapphistory.data.db.entity.AppUsageEventEntity
import com.example.yourapphistory.domain.model.AppUsageInfo
import com.example.yourapphistory.presentation.convertToRealUsageTime
import com.example.yourapphistory.presentation.toMillis
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ApplicationInfoSource @Inject constructor(
    private val context: Context
) {

    fun getLauncherAppInfoList(
        localDate: LocalDate
    ): List<AppInfo> {
        val appInfoMap: HashMap<String, Long> = hashMapOf()

        getUsageEventList(
            context = context,
            launcherAppList = getLauncherPackageNameList(context),
            beginDate = localDate.atStartOfDay().toMillis(),
            endDate = localDate.atTime(LocalTime.MAX).toMillis()
        ).forEach { appUsageInfo ->
            if (appInfoMap.containsKey(appUsageInfo.packageName)) {
                appInfoMap[appUsageInfo.packageName] =
                    appInfoMap[appUsageInfo.packageName]!! + (appUsageInfo.endTime - appUsageInfo.beginTime)
            } else {
                appInfoMap[appUsageInfo.packageName] =
                    appUsageInfo.endTime - appUsageInfo.beginTime
            }
        }

        return appInfoMap.map {
            AppInfo(
                packageName = it.key,
                appLabel = getApplicationLabel(context, it.key),
                appIcon = getApplicationIcon(context, it.key),
                todayUsageTime = it.value
            )
        }.sortedByDescending { it.todayUsageTime }
    }

    private fun getLauncherPackageNameList(
        context: Context,
    ): List<String> {
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

    private fun getApplicationLabel(
        context: Context,
        packageName: String
    ): String {
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

    private fun getApplicationIcon(
        context: Context,
        packageName: String
    ): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private suspend fun insertAppUsage(
    appListPackageNames: List<String>,
    usageEventList: List<AppUsageEventEntity>
    ) {
        var prevPackageName: String? = null
        var prevActivityClassName: String? = null
        val inCompletedUsageList: HashMap<String, AppUsageInfo> = hashMapOf()
        var isScreenOff: Boolean = false
        val completedUsageList: ArrayList<AppUsageInfo> = arrayListOf()

        for (usageEvent in usageEventList) {
            when (usageEvent.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (inCompletedUsageList[usageEvent.packageName] == null) {
                        inCompletedUsageList[usageEvent.packageName] =
                            AppUsageInfo(
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
                            endTime = usageEvent.eventTime
                        )
                    }

                    if (isScreenOff) {
                        if (appListPackageNames.contains(usageEvent.packageName)) {
                            if (inCompletedUsageList.containsKey(usageEvent.packageName)) {
                                if (inCompletedUsageList[usageEvent.packageName]!!.endTime.isZero()) {
                                    inCompletedUsageList[usageEvent.packageName] =
                                        inCompletedUsageList[usageEvent.packageName]!!.copy(
                                            endTime = usageEvent.eventTime
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
                        if (appListPackageNames.contains(usageEvent.packageName)) {
                            if (inCompletedUsageList[usageEvent.packageName]!!.endTime.isZero()) {
                                inCompletedUsageList[usageEvent.packageName] =
                                    inCompletedUsageList[usageEvent.packageName]!!.copy(
                                        endTime = usageEvent.eventTime
                                    )
                            }

                            completedUsageList.add(inCompletedUsageList[usageEvent.packageName]!!)
                        }

                        inCompletedUsageList.remove(usageEvent.packageName)
                    }
                }

                // 화면 꺼짐
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    prevPackageName = usageEvent.packageName
                    isScreenOff = true
                }

                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    isScreenOff = false
                }
            }
        }

        appUsageDao.insert(
            *completedUsageList.map {
                AppUsageEntity(
                    packageName = it.packageName,
                    startUseTime = it.startUseTime,
                    endUseTime = it.endUseTime,
                    totalUsedTime = it.endUseTime - it.startUseTime,
                    createTime = System.currentTimeMillis()
                )
            }.toTypedArray()
        )


        for (i in Const.FIRST_COLLECT_DAY downTo 0) {
            val localDate = LocalDate.now().minusDays(i)
            val range = localDate.atTime(LocalTime.MIN)..localDate.atTime(LocalTime.MAX)

            appUsageSummaryDao.insert(
                *completedUsageList.filter { range.contains(it.startUseTime.toLocalDateTime()) }
                    .groupingBy { it.packageName }
                    .fold(0L) { totalTime, usageInfo ->
                        totalTime + (usageInfo.endUseTime - usageInfo.startUseTime)
                    }.map {
                        AppUsageSummaryEntity(
                            date = localDate.toMillis(),
                            packageName = it.key,
                            totalUsageTime = it.value
                        )
                    }.toTypedArray()
            )
        }

        if (inCompletedUsageList.isNotEmpty()) {
            appUsageEventDao.deleteLegacy(
                inCompletedUsageList.minBy { it.value.startUseTime }.value.startUseTime
            )
        } else {

        }
    }

    private suspend fun insertUsageEvent(
        beginTime: Long,
        endTime: Long
    ) {
        val usageEvents: UsageEvents =
            (context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager).run {
                queryEvents(beginTime, endTime)
            }

        val resultArr: ArrayList<AppUsageEventEntity> = arrayListOf()

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
            ) {
//                Log.e(
//                    "APP_CHS_123",
//                    "${time.toSimpleDateConvert()}- $packageName : ${currentEvent.className}  -> $eventType"
//                )

                resultArr.add(
                    AppUsageEventEntity(
                        eventTime = time,
                        packageName = packageName,
                        className = currentEvent.className,
                        eventType = eventType
                    )
                )
            }
        }
        appUsageEventDao.insert(*resultArr.toTypedArray())
    }

    private fun isRealUsedPackage(packageUsageInfo: AppUsageInfo): Boolean {
//        return (packageUsageInfo.endTime - packageUsageInfo.beginTime) > 1000L
        return packageUsageInfo.endTime != 0L
    }
}
