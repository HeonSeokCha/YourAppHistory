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
import com.chs.yourapphistory.data.db.dao.UsageStateEventDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.UsageStateEventEntity
import com.chs.yourapphistory.data.paging.GetPagingForegroundList
import com.chs.yourapphistory.data.paging.GetPagingLaunchList
import com.chs.yourapphistory.data.paging.GetPagingNotifyList
import com.chs.yourapphistory.data.paging.GetPagingUsedList
import com.chs.yourapphistory.data.paging.GetPagingDailyAppForegroundInfo
import com.chs.yourapphistory.data.paging.GetPagingDailyAppLaunchInfo
import com.chs.yourapphistory.data.paging.GetPagingDailyAppNotifyInfo
import com.chs.yourapphistory.data.paging.GetPagingDailyAppUsedInfo
import com.chs.yourapphistory.data.paging.GetPagingWeekAppForegroundInfo
import com.chs.yourapphistory.data.paging.GetPagingWeekAppLaunchInfo
import com.chs.yourapphistory.data.paging.GetPagingWeekAppNotifyInfo
import com.chs.yourapphistory.data.paging.GetPagingWeekAppUsedInfo
import com.chs.yourapphistory.data.DataStoreSource
import com.chs.yourapphistory.data.db.dao.InCompleteAppUsageDao
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.model.AppUsageEventRawInfo
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
    private val appNotifyInfoDao: AppNotifyInfoDao,
    private val usageStateEventDao: UsageStateEventDao,
    private val dataStoreSource: DataStoreSource,
    private val inCompleteAppUsageDao: InCompleteAppUsageDao
) : AppRepository {

    private val mutex: Mutex by lazy { Mutex() }

    private suspend fun getLastEventTime(): Long {
        return withContext(Dispatchers.IO) {
            awaitAll(
                async { appUsageDao.getLastTime() },
                async { appForegroundUsageDao.getLastTime() },
                async { appNotifyInfoDao.getLastTime() },
                async { inCompleteAppUsageDao.getMinBeginTime() }
            ).min()
        }.run {
            if (this == 0L) {
                LocalDate.now().minusDays(Constants.FIRST_COLLECT_DAY).atStartOfDayToMillis()
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
        }.map { it.packageName }

        if (removePackageList.isEmpty()) return

        deleteUsageInfoFromPackageNames(removePackageList)
    }

    override suspend fun insertAppUsageInfo() {
        mutex.withLock {
//            withContext(Dispatchers.IO) {
//                appUsageDao.deleteAll()
//                appForegroundUsageDao.deleteAll()
//                appNotifyInfoDao.deleteAll()
//            }

            val installPackageNames = applicationInfoSource.getInstalledLauncherPackageNameList()
            val rangeList = applicationInfoSource.getUsageEvent(getLastEventTime())

//            val rangeList = usageStateEventDao.getAll().map {
//                AppUsageEventRawInfo(
//                    packageName = it.packageName,
//                    className = it.className,
//                    eventType = it.eventType,
//                    eventTime = it.eventTime
//                )
//            }

            withContext(Dispatchers.IO) {
                val usageStateEvent = async(Dispatchers.IO) {
                    usageStateEventDao.upsert(
                        *rangeList.map {
                            UsageStateEventEntity(
                                packageName = it.packageName,
                                className = it.className,
                                eventTime = it.eventTime,
                                eventType = it.eventType
                            )
                        }.toTypedArray()
                    )
                }

                val appUsageInsert = async(Dispatchers.IO) {
                    applicationInfoSource.getAppUsageInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = rangeList.filter { rawInfo ->
                            Constants.APP_USAGE_EVENT_FILTER.any { it == rawInfo.eventType }
                        }
                    ).run {
                        val usageList: List<AppUsageEntity> = this.first
                        if (dataStoreSource.getData(Constants.PREF_KEY_FIRST_DATE) == null) {
                            updateFirstCollectDate(usageList.minBy { it.beginUseTime }.beginUseTime)
                        }

                        appUsageDao.upsert(*usageList.toTypedArray())

                        val incompList = this.second.ifEmpty { return@async }
//                        inCompleteAppUsageDao.upsert(*incompList.toTypedArray())
                    }
                }

                val appForegroundUsageInsert = async(Dispatchers.IO) {
                    applicationInfoSource.getAppForeGroundUsageInfoList(
                        installPackageNames = installPackageNames,
                        usageEventList = rangeList.filter { rawInfo ->
                            Constants.APP_FOREGROUND_USAGE_EVENT_FILTER.any { it == rawInfo.eventType }
                        }
                    ).run {
                        val foregroundUsageList = this.first
                        appForegroundUsageDao.upsert(*foregroundUsageList.toTypedArray())

                        val incompList = this.second.ifEmpty { return@async }
//                        inCompleteAppUsageDao.upsert(*incompList.toTypedArray())
                    }
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
                awaitAll(
                    usageStateEvent,
                    appUsageInsert,
                    appForegroundUsageInsert,
                    appNotifyInfoUpsert
                )
            }
        }
    }

    override suspend fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        val minDate: LocalDate = getMinDate()
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
        val minDate: LocalDate = getMinDate()
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
        val minDate: LocalDate = getMinDate()
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
        val minDate: LocalDate = getMinDate()
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
        val minDate: LocalDate = getMinDate()
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
        val minDate: LocalDate = getMinDate()
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
        val minDate: LocalDate = getMinDate()
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
        val minDate: LocalDate = getMinDate()
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
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>> {
        val minDate: LocalDate = getMinDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingWeekAppUsedInfo(
                dao = appUsageDao,
                minDate = minDate,
                targetDate = targetDate,
                packageName = packageName
            )
        }.flow
    }

    override suspend fun getWeeklyPagingAppForegroundInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>> {
        val minDate: LocalDate = getMinDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingWeekAppForegroundInfo(
                dao = appForegroundUsageDao,
                minDate = minDate,
                targetDate = targetDate,
                packageName = packageName
            )
        }.flow
    }

    override suspend fun getWeeklyPagingAppLaunchInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>> {
        val minDate: LocalDate = getMinDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingWeekAppLaunchInfo(
                dao = appUsageDao,
                minDate = minDate,
                targetDate = targetDate,
                packageName = packageName
            )
        }.flow
    }

    override suspend fun getWeeklyPagingAppNotifyInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>> {
        val minDate: LocalDate = getMinDate()
        return Pager(
            PagingConfig(pageSize = Constants.PAGING_DAY.toInt())
        ) {
            GetPagingWeekAppNotifyInfo(
                dao = appNotifyInfoDao,
                minDate = minDate,
                targetDate = targetDate,
                packageName = packageName
            )
        }.flow
    }

    override suspend fun getAppIconMap(): HashMap<String, Bitmap?> {
        return applicationInfoSource.getApplicationIconMap(
            applicationInfoSource.getInstalledLauncherPackageNameList()
        )
    }

    override suspend fun getMinDate(): LocalDate {
        return dataStoreSource.getData(Constants.PREF_KEY_FIRST_DATE)?.toLocalDate()
            ?: LocalDate.now()
    }

    override suspend fun deleteUsageInfo(packageName: String) {
        deleteUsageInfoFromPackageNames(listOf(packageName))
    }

    private suspend fun deleteUsageInfoFromPackageNames(packageNames: List<String>) {
        appInfoDao.deleteFromPackageName(packageNames)
        appUsageDao.deleteFromPackageName(packageNames)
        appForegroundUsageDao.deleteFromPackageName(packageNames)
        appNotifyInfoDao.deleteFromPackageName(packageNames)
    }

    private suspend fun updateFirstCollectDate(dateMilli: Long) {
        dataStoreSource.updateData(
            key = Constants.PREF_KEY_FIRST_DATE,
            value = dateMilli
        )
    }

}