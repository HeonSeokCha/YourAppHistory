package com.example.yourapphistory

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build

object Util {

    fun getLauncherAppList(context: Context): HashMap<String, Drawable> {
        val appInfoMap: HashMap<String, Drawable> = hashMapOf()

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
            appInfoMap[getApplicationLabel(context, it.activityInfo.packageName)] =
                context.packageManager.getApplicationIcon(it.activityInfo.packageName)
        }

        return appInfoMap
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
            ).toString()
        } else {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(
                    packageName,
                    0
                )
            ).toString()
        }
    }

    fun getPackageUsageList(
        context: Context,
        beginDate: Long,
        endDate: Long,
        targetPackageName: String? = null
    ): List<AppUsageInfo> {

        val packageNameSets: HashSet<String> = if (targetPackageName != null) {
            getLauncherAppList(context).keys.filter { it == targetPackageName }.toHashSet()
        } else {
            getLauncherAppList(context).keys.toHashSet()
        }

        return getUsageEventList(
            context = context,
            beginDate = beginDate,
            endDate = endDate,
            packageNameSets = packageNameSets
        )
    }

    private fun getUsageEventList(
        context: Context,
        beginDate: Long,
        endDate: Long,
        packageNameSets: HashSet<String>
    ): List<AppUsageInfo> {

        val usm: UsageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageEvents = usm.queryEvents(beginDate, endDate)
        val eventUsage: HashMap<String, AppUsageInfo> = hashMapOf()
        val totalUsage: ArrayList<AppUsageInfo> = arrayListOf()
        var prevEventPackageName: String? = null
        val unFinishedPackageList: ArrayList<String> = arrayListOf()

        while (usageEvents.hasNextEvent()) {
            val currentEvent = UsageEvents.Event().apply {
                usageEvents.getNextEvent(this)
            }

            val packageName: String? = currentEvent.packageName
            val time: Long = currentEvent.timeStamp

            if (packageName == null) continue

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
                            if (packageNameSets.contains(prevEventPackageName)) {
                                unFinishedPackageList.add(prevEventPackageName)
                                eventUsage[prevEventPackageName] =
                                    eventUsage[prevEventPackageName]!!.copy(
                                        endTime = time
                                    )
                            } else {
                                eventUsage.remove(prevEventPackageName)
                            }
                        } else {
                            if (packageNameSets.contains(prevEventPackageName)) {
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
            if (packageNameSets.contains(eventUsage.key)) {
                if (isRealUsedPackage(eventUsage.value)) {
                    totalUsage.add(eventUsage.value)
                }
            }
        }
        eventUsage.clear()
        unFinishedPackageList.clear()

        return totalUsage.sortedBy { it.beginTime }
    }


    private fun isRealUsedPackage(packageUsageInfo: AppUsageInfo): Boolean {
        return (packageUsageInfo.endTime - packageUsageInfo.beginTime) > 1000L
                && packageUsageInfo.endTime != 0L
    }
}