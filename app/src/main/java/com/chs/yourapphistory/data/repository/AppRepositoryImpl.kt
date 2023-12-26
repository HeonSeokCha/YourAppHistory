package com.chs.yourapphistory.data.repository

import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atEndOfDayToMillis
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.isZero
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.toAppInfo
import com.chs.yourapphistory.data.toAppUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val applicationInfoSource: ApplicationInfoSource,
    private val appUsageDao: AppUsageDao,
    private val appInfoDao: AppInfoDao
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
        val localList: List<String> = appInfoDao.getAllPackageNames()
        val currentLauncherList: List<String> =
            applicationInfoSource.getInstalledLauncherPackageNameList()
        appInfoDao.insert(
            *currentLauncherList
                .filterNot { launcherPackageName ->
                    localList.any { it == launcherPackageName }
                }.map { packageName ->
                    AppInfoEntity(
                        packageName = packageName,
                        label = applicationInfoSource.getApplicationLabel(packageName),
                    )
                }.toTypedArray()
        )

        localList.filterNot { launcherPackageName ->
            currentLauncherList.any { it == launcherPackageName }
        }.map { packageName ->
            packageName
        }.forEach {
            appInfoDao.deleteAppInfo(it)
        }
    }

    override suspend fun insertAppUsageInfo() {
        val rawEvents = applicationInfoSource.getUsageEvent(
            getLastUsageEventTime()
        )

        appUsageDao.insert(
            *applicationInfoSource.getAppUsageInfoList(rawEvents).toTypedArray()
        )
    }

    override fun getDayUsedAppInfoList(date: LocalDate): Flow<List<Pair<AppInfo, List<AppUsageInfo>>>> {
        return appInfoDao.getDayUsedAppInfoList(
            beginTime = date.atStartOfDayToMillis(),
            endTime = date.atEndOfDayToMillis()
        ).map {
            it.map {
                it.key.toAppInfo(
                    applicationInfoSource.getApplicationIcon(it.key.packageName)
                ) to it.value.map {
                    it.toAppUsageInfo()
                }
            }
        }
    }

    override suspend fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): List<AppUsageInfo> {
        return appUsageDao.getUsageInfoList(
            beginTime = date.atStartOfDayToMillis(),
            endTime = date.atEndOfDayToMillis(),
            packageName = packageName
        ).map { it.toAppUsageInfo() }
    }

    override suspend fun getOldestAppUsageCollectDay(): LocalDate {
        val oldestCollectTime: Long = appUsageDao.getOldestCollectTime()
        return if (oldestCollectTime.isZero()) {
            LocalDate.now().minusDays(Constants.FIRST_COLLECT_DAY)
        } else {
            oldestCollectTime.toLocalDate()
        }
    }
}