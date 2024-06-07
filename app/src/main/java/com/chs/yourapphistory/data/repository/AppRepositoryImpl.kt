package com.chs.yourapphistory.data.repository

import android.graphics.Bitmap
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.model.UsageEventType
import com.chs.yourapphistory.data.paging.GetDayPagingForegroundUsedList
import com.chs.yourapphistory.data.paging.GetDayPagingNotifyList
import com.chs.yourapphistory.data.paging.GetDayPagingUsedList
import com.chs.yourapphistory.data.toAppForegroundUsageInfo
import com.chs.yourapphistory.data.toAppNotifyInfo
import com.chs.yourapphistory.data.toAppUsageInfo
import com.chs.yourapphistory.domain.model.AppBaseUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppNotifyInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val applicationInfoSource: ApplicationInfoSource,
    private val appUsageDao: AppUsageDao,
    private val appInfoDao: AppInfoDao,
    private val appForegroundUsageDao: AppForegroundUsageDao,
    private val appNotifyInfoDao: AppNotifyInfoDao
) : AppRepository {

    private suspend fun getLastEventTime(): Long {
        return listOf(
            appUsageDao.getLastEventTime(),
            appForegroundUsageDao.getLastEventTime(),
            appNotifyInfoDao.getLastEventTime()
        ).min().run {
            if (this == 0L) {
                LocalDate.now().minusDays(Constants.FIRST_COLLECT_DAY).toMillis()
            } else this
        }
    }

    override suspend fun insertInstallAppInfo() {
        val localList: List<AppInfoEntity> = appInfoDao.getAllPackage()
        val currentLauncherList: List<String> =
            applicationInfoSource.getInstalledLauncherPackageNameList()

        appInfoDao.upsert(
            *currentLauncherList
                .filterNot { launcherPackageName ->
                    localList.any {
                        it.packageName == launcherPackageName
                                && it.label == getPackageLabel(launcherPackageName)
                    }
                }.map { packageName ->
                    AppInfoEntity(
                        packageName = packageName,
                        label = applicationInfoSource.getApplicationLabel(packageName),
                    )
                }.toTypedArray()
        )

        val removePackageList = localList.filterNot { packageInfo ->
            currentLauncherList.any { it == packageInfo.packageName }
        }

        if (removePackageList.isEmpty()) return

        appInfoDao.delete(*removePackageList.toTypedArray())
        appUsageDao.deleteUsageInfo(removePackageList.map { it.packageName })

    }

    override suspend fun insertAppUsageInfo() {
        val installPackageNames = applicationInfoSource.getInstalledLauncherPackageNameList()
        val rangeList = applicationInfoSource.getUsageEvent(getLastEventTime())

        withContext(Dispatchers.IO) {
            val appUsageInsert = async(Dispatchers.IO) {
                appUsageDao.upsert(
                    *applicationInfoSource.getAppUsageInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = rangeList.filter { rawInfo ->
                            Constants.APP_USAGE_EVENT_FILTER.any { it == rawInfo.eventType }
                        }
                    ).toTypedArray()
                )
            }

            val appForegroundUsageInsert = async(Dispatchers.IO) {
                appForegroundUsageDao.upsert(
                    *applicationInfoSource.getAppForeGroundUsageInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = rangeList.filter { rawInfo ->
                            Constants.APP_FOREGROUND_USAGE_EVENT_FILTER.any { it == rawInfo.eventType }
                        }
                    ).toTypedArray()
                )
            }

            val appNotifyInfoUpsert = async(Dispatchers.IO) {
                appNotifyInfoDao.upsert(
                    *applicationInfoSource.getAppNotifyInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = rangeList.filter { rawInfo ->
                            Constants.APP_NOTIFY_EVENT_FILTER.any { it == rawInfo.eventType }
                        }
                    ).toTypedArray()
                )
            }
            awaitAll(appUsageInsert, appForegroundUsageInsert, appNotifyInfoUpsert)
        }
    }

    override fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, List<Pair<Long, Long>>>>>>> {
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetDayPagingUsedList(appInfoDao = appInfoDao)
        }.flow
    }

    override fun getDayForegroundUsedAppList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, List<Pair<Long, Long>>>>>>> {
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetDayPagingForegroundUsedList(appInfoDao = appInfoDao)
        }.flow
    }

    override fun getDayNotifyAppList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetDayPagingNotifyList(appInfoDao = appInfoDao)
        }.flow
    }

    override suspend fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): List<AppBaseUsageInfo.AppUsageInfo> {
        return appUsageDao.getDayUsageInfoList(
            targetDate = date.toMillis(),
            packageName = packageName
        ).map {
            it.toAppUsageInfo()
        }
    }

    override suspend fun getAppForegroundUsageInfoList(
        date: LocalDate,
        packageName: String
    ): List<AppBaseUsageInfo.AppForegroundUsageInfo> {
        return appForegroundUsageDao.getDayForegroundUsageInfo(
            targetDate = date.toMillis(),
            packageName = packageName
        ).map {
            it.toAppForegroundUsageInfo()
        }
    }

    override suspend fun getAppNotifyInfoList(
        date: LocalDate,
        packageName: String
    ): List<AppNotifyInfo> {
        return appNotifyInfoDao.getDayNotifyCount(
            packageName = packageName,
            targetDate = date.toMillis()
        ).map {
            it.toAppNotifyInfo()
        }
    }

    override suspend fun getOldestAppUsageCollectDay(): LocalDate {
        return appUsageDao.getFirstCollectTime().toLocalDate()
    }

    override suspend fun getPackageLabel(packageName: String): String {
        return applicationInfoSource.getApplicationLabel(packageName)
    }

    override suspend fun getAppIconMap(): HashMap<String, Bitmap?> {
        return applicationInfoSource.getApplicationIconMap(
            applicationInfoSource.getInstalledLauncherPackageNameList()
        )
    }
}