package com.chs.yourapphistory.data.repository

import android.graphics.Bitmap
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.toConvertDayUsedTime
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.dao.UsageStateEventDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.UsageStateEventEntity
import com.chs.yourapphistory.data.DataStoreSource
import com.chs.yourapphistory.data.db.dao.InCompleteAppUsageDao
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.paging.GetPagingDailyAppInfos
import com.chs.yourapphistory.data.paging.GetPagingWeeklyAppInfos
import com.chs.yourapphistory.data.paging.GetPagingWeeklyTotalAppInfo
import com.chs.yourapphistory.data.toAppInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.time.LocalDate

@Single
class AppRepositoryImpl(
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
            ).min()
        }.run {
            if (this == 0L) {
                LocalDate.now().minusDays(Constants.FIRST_COLLECT_DAY).atStartOfDayToMillis()
            } else this
        }
    }

    override suspend fun insertInstallAppInfo() {
        chsLog("START insertInstallAppInfo")
        val localList: List<AppInfoEntity> = appInfoDao.getPackageDetailInfo()
        val currentPackageNameList: List<String> =
            applicationInfoSource.getInstalledLauncherPackageNameList()

        appInfoDao.upsert(
            *currentPackageNameList
                .filterNot { launcherPackageName ->
                    localList.any { it.packageName == launcherPackageName }
                }.map { packageName ->
                    AppInfoEntity(
                        packageName = packageName,
                        label = applicationInfoSource.getApplicationLabel(packageName),
                        firstInstallTime = applicationInfoSource.getFirstInstallTime(packageName),
                        lastUpdateTime = applicationInfoSource.getLastUpdateTime(packageName),
                        installProvider = applicationInfoSource.getInstallProvider(packageName),
                        lastUsedTime = null,
                        lastForegroundUsedTime = null
                    )
                }.toTypedArray()
        )

        val currentPackageInfo = currentPackageNameList.map { packageName ->
            AppInfoEntity(
                packageName = packageName,
                label = applicationInfoSource.getApplicationLabel(packageName),
                firstInstallTime = applicationInfoSource.getFirstInstallTime(packageName),
                lastUpdateTime = applicationInfoSource.getLastUpdateTime(packageName),
                installProvider = applicationInfoSource.getInstallProvider(packageName),
                lastUsedTime = null,
                lastForegroundUsedTime = null
            )
        }

        appInfoDao.update(
            *currentPackageInfo.filterNot { currentPackageInfo ->
                localList.any { localPackageInfo ->
                    currentPackageInfo.packageName == localPackageInfo.packageName
                            && currentPackageInfo.lastUpdateTime == localPackageInfo.lastUpdateTime
                            && currentPackageInfo.installProvider == localPackageInfo.installProvider
                }
            }.toTypedArray()
        )

        val removePackageList = localList.filterNot { packageInfo ->
            currentPackageNameList.any { it == packageInfo.packageName }
        }.map { it.packageName }.ifEmpty { return }

        deleteUsageInfoFromPackageNames(removePackageList)
        chsLog("END insertInstallAppInfo")
    }

    override suspend fun getDayUsedAppInfoList(targetDateMilli: Long): List<Pair<AppInfo, Int>> {
        val iconMap = getAppIconMap()
        val targetDate = targetDateMilli.toLocalDate()
        return appUsageDao.getDayAppUsedInfo(targetDateMilli).map {
            it.key.toAppInfo(iconMap[it.key.packageName]) to it.value.toConvertDayUsedTime(
                targetDate
            )
        }.sortedWith(compareBy({ -it.second }, { it.first.label }))
    }

    override suspend fun getDayForegroundUsedAppList(targetDateMilli: Long): List<Pair<AppInfo, Int>> {
        val iconMap = getAppIconMap()
        val targetDate = targetDateMilli.toLocalDate()
        return appForegroundUsageDao.getDayForegroundUsedList(targetDateMilli).map {
            it.key.toAppInfo(icon = iconMap[it.key.packageName]) to
                    it.value.toConvertDayUsedTime(targetDate)
        }.sortedWith(
            compareBy(
                { -it.second },
                { it.first.label }
            )
        )
    }

    override suspend fun getDayNotifyAppList(targetDateMilli: Long): List<Pair<AppInfo, Int>> {
        val iconMap = getAppIconMap()
        return appNotifyInfoDao.getDayNotifyList(targetDateMilli).map {
            it.key.toAppInfo(icon = iconMap[it.key.packageName]) to it.value
        }.sortedWith(
            compareBy(
                { -it.second },
                { it.first.label }
            )
        )
    }

    override suspend fun getDayLaunchAppList(targetDateMilli: Long): List<Pair<AppInfo, Int>> {
        val iconMap = getAppIconMap()
        return appUsageDao.getDayAppLaunchInfo(targetDateMilli).map {
            it.key.toAppInfo(icon = iconMap[it.key.packageName]) to it.value
        }
    }

    override suspend fun insertAppUsageInfo() {
        mutex.withLock {
            chsLog("START insertAppUsageInfo")
//            withContext(Dispatchers.IO) {
//                appUsageDao.deleteAll()
//                appForegroundUsageDao.deleteAll()
//                appNotifyInfoDao.deleteAll()
//                inCompleteAppUsageDao.deleteAll()
//            }
//            chsLog("DELETE ALL")

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
                        val incompList = this.second
                        if (dataStoreSource.getData(Constants.PREF_KEY_FIRST_DATE) == null) {
                            updateFirstCollectDate(usageList.minBy { it.beginUseTime }.beginUseTime)
                        } else {
                            inCompleteAppUsageDao.upsert(*incompList.toTypedArray())
                        }

                        inCompleteAppUsageDao.delete(
                            *inCompleteAppUsageDao.getListFromType("FG").filter {
                                usageList.any { usage ->
                                    usage.packageName == it.packageName
                                            && usage.beginUseTime == it.beginUseTime
                                }
                            }.toTypedArray()
                        )

                        val a = usageList.groupBy { it.packageName }
                            .map { it.key to it.value.maxOf { it.endUseTime } }

                        a.forEach {
                            appInfoDao.updateLastUsedTime(it.first, it.second)
                        }

                        appUsageDao.upsert(*usageList.toTypedArray())
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


                        val a = foregroundUsageList.groupBy { it.packageName }
                            .map { it.key to it.value.maxOf { it.endUseTime } }


                        a.forEach {
                            appInfoDao.updateLastForegroundUsedTime(it.first, it.second)
                        }

                        if (dataStoreSource.getData(Constants.PREF_KEY_FIRST_DATE) == null) return@run

                        inCompleteAppUsageDao.delete(
                            *inCompleteAppUsageDao.getListFromType("BG").filter {
                                foregroundUsageList.any { usage ->
                                    usage.packageName == it.packageName
                                            && usage.beginUseTime == it.beginUseTime
                                }
                            }.toTypedArray()
                        )

                        val incompList = this.second.ifEmpty { return@run }

                        inCompleteAppUsageDao.upsert(*incompList.toTypedArray())
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
            chsLog("END insertAppUsageInfo")
        }
    }


    override fun getWeeklyPagingAppInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Map<SortType, List<Pair<LocalDate, Int>>>>> = flow {
        emit(getMinDate())
    }.flatMapLatest {
        Pager(
            PagingConfig(
                pageSize = Constants.PAGING_DAY.toInt(),
                enablePlaceholders = false
            )
        ) {
            GetPagingWeeklyAppInfos(
                minDate = it,
                targetDate = targetDate,
                packageName = packageName,
                appUsageDao = appUsageDao,
                appForegroundDao = appForegroundUsageDao,
                appNotifyInfoDao = appNotifyInfoDao
            )
        }.flow
    }

    override fun getDailyPagingAppInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Map<SortType, List<Pair<Int, Int>>>>> = flow {
        emit(getMinDate())
    }.flatMapLatest {
        Pager(
            PagingConfig(
                pageSize = Constants.PAGING_DAY.toInt(),
                enablePlaceholders = false
            )
        ) {
            GetPagingDailyAppInfos(
                minDate = it,
                targetDate = targetDate,
                packageName = packageName,
                appUsageDao = appUsageDao,
                appForegroundDao = appForegroundUsageDao,
                appNotifyInfoDao = appNotifyInfoDao
            )
        }.flow
    }

    override fun getWeeklyPagingTotalAppInfo(): Flow<PagingData<Map<SortType, List<Pair<LocalDate, List<AppTotalUsageInfo>>>>>> =
        flow {
            emit(getMinDate())
        }.flatMapLatest {
            Pager(
                PagingConfig(
                    pageSize = Constants.PAGING_DAY.toInt(),
                    enablePlaceholders = false
                )
            ) {
                GetPagingWeeklyTotalAppInfo(
                    minDate = it,
                    appUsageDao = appUsageDao,
                    appNotifyInfoDao = appNotifyInfoDao
                )
            }.flow
        }

    private suspend fun getAppIconMap(): HashMap<String, Bitmap?> {
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