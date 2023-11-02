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
import com.example.yourapphistory.common.toMillis
import com.example.yourapphistory.data.db.dao.AppUsageDao
import com.example.yourapphistory.data.db.dao.AppUsageEventDao
import com.example.yourapphistory.data.db.entity.AppUsageEntity
import com.example.yourapphistory.data.db.entity.AppUsageEventEntity
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationInfoSource @Inject constructor(private val context: Context) {

    private fun getInstalledLauncherPackageNameList(): List<String> {
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

    fun getApplicationIcon(packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    suspend fun getAppUsageInfoList(
        installPackageNames: List<String>,
        usageEventList: List<AppUsageEventEntity>
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

        return completedUsageList
    }

    suspend fun getUsageEvent(beginTime: Long): List<AppUsageEventEntity> {
        val usageEvents: UsageEvents =
            (context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager).run {
                queryEvents(beginTime, System.currentTimeMillis())
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
        return resultArr
    }
}
