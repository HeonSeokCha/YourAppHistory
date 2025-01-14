package com.chs.yourapphistory.data.repository

import android.graphics.Bitmap
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.paging.GetPagingForegroundList
import com.chs.yourapphistory.data.paging.GetPagingLaunchList
import com.chs.yourapphistory.data.paging.GetPagingNotifyList
import com.chs.yourapphistory.data.paging.GetPagingUsedList
import com.chs.yourapphistory.data.paging.GetPagingDailyAppForegroundInfo
import com.chs.yourapphistory.data.paging.GetPagingDailyAppLaunchInfo
import com.chs.yourapphistory.data.paging.GetPagingDailyAppNotifyInfo
import com.chs.yourapphistory.data.paging.GetPagingDailyAppUsedInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val mutex: Mutex by lazy { Mutex() }

    private suspend fun getLastEventTime(): Long {
        return withContext(Dispatchers.IO) {
            awaitAll(
                async { appUsageDao.getLastEventTime() },
                async { appNotifyInfoDao.getLastEventTime() },
                async { appForegroundUsageDao.getLastEventTime() }
            ).min().run {
                if (this == 0L) {
                    LocalDate.now().minusDays(Constants.FIRST_COLLECT_DAY).atStartOfDayToMillis()
                } else this
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
                                && it.label == applicationInfoSource.getApplicationLabel(
                            launcherPackageName
                        )
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
        mutex.withLock {
//            withContext(Dispatchers.IO) { appUsageDao.deleteAllUsageInfo() }

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
    }

    override suspend fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        val minDate: LocalDate = appUsageDao.getFirstCollectTime().toLocalDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingUsedList(
                appUsageDao = appUsageDao,
                minDate = minDate
            )
        }.flow
    }


    override suspend fun getDayForegroundUsedAppList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        val minDate: LocalDate = appForegroundUsageDao.getFirstCollectTime().toLocalDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingForegroundList(
                appForegroundUsageDao = appForegroundUsageDao,
                minDate = minDate
            )
        }.flow
    }

    override suspend fun getDayNotifyAppList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        val minDate: LocalDate = appNotifyInfoDao.getFirstCollectTime().toLocalDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingNotifyList(
                appNotifyInfoDao = appNotifyInfoDao,
                minDate = minDate
            )
        }.flow
    }

    override suspend fun getDayLaunchAppList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        val minDate: LocalDate = appUsageDao.getFirstCollectTime().toLocalDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingLaunchList(
                appUsageDao = appUsageDao,
                minDate = minDate
            )
        }.flow
    }

    override suspend fun getDailyPagingAppUsedInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        val minDate = appUsageDao.getFirstCollectTime().toLocalDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingDailyAppUsedInfo(
                dao = appUsageDao,
                minDate = minDate,
                targetDate = targetDate,
                packageName = packageName
            )
        }.flow
    }

    override suspend fun getDailyPagingAppForegroundInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        val minDate = appForegroundUsageDao.getFirstCollectTime().toLocalDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingDailyAppForegroundInfo(
                dao = appForegroundUsageDao,
                minDate = minDate,
                targetDate = targetDate,
                packageName = packageName
            )
        }.flow
    }

    override suspend fun getDailyPagingAppLaunchInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        val minDate = appUsageDao.getFirstCollectTime().toLocalDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingDailyAppLaunchInfo(
                dao = appUsageDao,
                minDate = minDate,
                targetDate = targetDate,
                packageName = packageName
            )
        }.flow
    }

    override suspend fun getDailyPagingAppNotifyInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        val minDate = appNotifyInfoDao.getFirstCollectTime().toLocalDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingDailyAppNotifyInfo(
                dao = appNotifyInfoDao,
                minDate = minDate,
                targetDate = targetDate,
                packageName = packageName
            )
        }.flow
    }

    override suspend fun getWeeklyPagingAppUsedInfo(
        beginDate: LocalDate,
        endDate: LocalDate,
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getWeeklyPagingAppForegroundInfo(
        beginDate: LocalDate,
        endDate: LocalDate,
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getWeeklyPagingAppLaunchInfo(
        beginDate: LocalDate,
        endDate: LocalDate,
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getWeeklyPagingAppNotifyInfo(
        beginDate: LocalDate,
        endDate: LocalDate,
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAppIconMap(): HashMap<String, Bitmap?> {
        return applicationInfoSource.getApplicationIconMap(
            applicationInfoSource.getInstalledLauncherPackageNameList()
        )
    }

    override suspend fun getMinDate(): LocalDate {
        return withContext(Dispatchers.IO) {
            awaitAll(
                async { appUsageDao.getFirstCollectTime() },
                async { appForegroundUsageDao.getFirstCollectTime() },
                async { appNotifyInfoDao.getFirstCollectTime() },
            )
                .min()
                .toLocalDate()
        }
    }
}