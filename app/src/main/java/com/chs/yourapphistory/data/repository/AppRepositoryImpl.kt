package com.chs.yourapphistory.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atEndOfDayToMillis
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
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

class AppRepositoryImpl @Inject constructor(
    private val applicationInfoSource: ApplicationInfoSource,
    private val appUsageDao: AppUsageDao,
    private val appInfoDao: AppInfoDao,
    private val appForegroundUsageDao: AppForegroundUsageDao,
    private val appNotifyInfoDao: AppNotifyInfoDao
) : AppRepository {

    private suspend fun getLastUsageEventTime(): Long {
        return appUsageDao.getLastEndUseTime().run {
            if (this == 0L) {
                LocalDate.now().minusDays(Constants.FIRST_COLLECT_DAY).toMillis()
            } else {
                this
            }
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
        val rawEvents = applicationInfoSource.getUsageEvent(
            getLastUsageEventTime()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val appUsageInsert = async {
                appUsageDao.upsert(
                    *applicationInfoSource.getAppUsageInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = rawEvents
                    ).toTypedArray()
                )
            }

            val appForegroundUsageInsert = async {
                appForegroundUsageDao.upsert(
                    *applicationInfoSource.getAppForeGroundUsageInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = rawEvents
                    ).toTypedArray()
                )
            }

            val appNotifyInfoUpsert = async {
                appNotifyInfoDao.upsert(
                    *applicationInfoSource.getAppNotifyInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = rawEvents
                    ).toTypedArray()
                )
            }
            awaitAll(appUsageInsert, appForegroundUsageInsert, appNotifyInfoUpsert)
        }
    }

    override fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, List<AppBaseUsageInfo.AppUsageInfo>>>>>> {
        return Pager(
            PagingConfig(
                pageSize = 1,
            )
        ) {
            GetDayPagingAppUsedInfo(
                appInfoDao = appInfoDao,
                applicationInfoSource = applicationInfoSource,
            )
        }.flow
    }

    override suspend fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): List<AppBaseUsageInfo.AppUsageInfo> {
        return appUsageDao.getDayUsageInfoList(
            beginDate = date.atStartOfDayToMillis(),
            endDate = date.atEndOfDayToMillis(),
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
            beginDate = date.atStartOfDayToMillis(),
            endDate = date.atEndOfDayToMillis(),
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