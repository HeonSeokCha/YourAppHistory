package com.chs.yourapphistory.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.dao.BaseDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.model.UsageEventType
import com.chs.yourapphistory.data.paging.GetDayPagingAppUsedInfo
import com.chs.yourapphistory.data.toAppForegroundUsageInfo
import com.chs.yourapphistory.data.toAppNotifyInfo
import com.chs.yourapphistory.data.toAppUsageInfo
import com.chs.yourapphistory.domain.model.AppBaseUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppNotifyInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class AppRepositoryImpl @Inject constructor(
    private val applicationInfoSource: ApplicationInfoSource,
    private val appUsageDao: AppUsageDao,
    private val appInfoDao: AppInfoDao,
    private val appForegroundUsageDao: AppForegroundUsageDao,
    private val appNotifyInfoDao: AppNotifyInfoDao
) : AppRepository {

    private suspend fun getLastEventTime(usageEventType: UsageEventType): Long {
        return when (usageEventType) {
            is UsageEventType.AppUsageEvent -> {
                appUsageDao.getLastEventTime()
            }

            is UsageEventType.AppForegroundUsageEvent -> {
                appForegroundUsageDao.getLastEventTime()
            }

            is UsageEventType.AppNotifyEvent -> {
                appNotifyInfoDao.getLastEventTime()
            }
        }.run {
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
        }.map { packageName ->
            packageName
        }

        withContext(Dispatchers.IO) {
            val (appInfo, appUsage) = async { appInfoDao.delete(*removePackageList.toTypedArray()) } to
                    async {
                        appUsageDao.delete(
                            *removePackageList.map {
                                AppUsageEntity(
                                    packageName = it.packageName,
                                    beginUseTime = 0L
                                )
                            }.toTypedArray()
                        )
                    }
            awaitAll(appInfo, appUsage)
        }
    }

    override suspend fun insertAppUsageInfo() {
        val installPackageNames = applicationInfoSource.getInstalledLauncherPackageNameList()
        withContext(Dispatchers.IO) {
            val appUsageInsert = async(Dispatchers.IO) {
                val type: UsageEventType = UsageEventType.AppUsageEvent
                appUsageDao.upsert(
                    *applicationInfoSource.getAppUsageInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = applicationInfoSource.getUsageEvent(
                            usageType = type,
                            beginTime = getLastEventTime(type)
                        )
                    ).toTypedArray()
                )
            }

            val appForegroundUsageInsert = async(Dispatchers.IO) {
                val type: UsageEventType = UsageEventType.AppForegroundUsageEvent
                appForegroundUsageDao.upsert(
                    *applicationInfoSource.getAppForeGroundUsageInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = applicationInfoSource.getUsageEvent(
                            usageType = type,
                            beginTime = getLastEventTime(type)
                        )
                    ).toTypedArray()
                )
            }

            val appNotifyInfoUpsert = async(Dispatchers.IO) {
                val type: UsageEventType = UsageEventType.AppNotifyEvent
                appNotifyInfoDao.upsert(
                    *applicationInfoSource.getAppNotifyInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = applicationInfoSource.getUsageEvent(
                            usageType = type,
                            beginTime = getLastEventTime(type)
                        )
                    ).toTypedArray()
                )
            }
            awaitAll(appUsageInsert, appForegroundUsageInsert, appNotifyInfoUpsert)
        }
    }

    override fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, List<AppBaseUsageInfo.AppUsageInfo>>>>>> {
        return Pager(
            PagingConfig(pageSize = Constants.FIRST_COLLECT_DAY.toInt())
        ) {
            GetDayPagingAppUsedInfo(
                appInfoDao = appInfoDao,
                applicationInfoSource = applicationInfoSource
            )
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
        return appUsageDao.getOldestCollectTime().toLocalDate()
    }

    override suspend fun getPackageLabel(packageName: String): String {
        return applicationInfoSource.getApplicationLabel(packageName)
    }
}