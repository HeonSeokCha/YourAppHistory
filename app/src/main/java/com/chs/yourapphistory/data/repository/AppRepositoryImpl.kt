package com.chs.yourapphistory.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.isZero
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.paging.GetDayPagingAppUsageInfo
import com.chs.yourapphistory.data.paging.GetDayPagingAppUsedInfo
import com.chs.yourapphistory.data.toAppUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import com.chs.yourapphistory.domain.usecase.GetAppUsageTimeZoneInfoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
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
        val localList: List<AppInfoEntity> = appInfoDao.getAllPackage()
        val currentLauncherList: List<String> =
            applicationInfoSource.getInstalledLauncherPackageNameList()
        appInfoDao.insert(
            *currentLauncherList
                .filterNot { launcherPackageName ->
                    localList.any { it.packageName == launcherPackageName }
                }.map { packageName ->
                    AppInfoEntity(
                        packageName = packageName,
                        label = applicationInfoSource.getApplicationLabel(packageName),
                    )
                }.toTypedArray()
        )

        localList.filterNot { packageInfo ->
            currentLauncherList.any { it == packageInfo.packageName }
        }.map { packageName ->
            packageName
        }.forEach {
            appInfoDao.deleteAppInfo(it.packageName)
            appUsageDao.deleteUsageInfo(it.packageName)
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

    override fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, List<AppUsageInfo>>>>>> {
        return Pager(
            PagingConfig(pageSize = Constants.FIRST_COLLECT_DAY.toInt())
        ) {
            GetDayPagingAppUsedInfo(
                appInfoDao = appInfoDao,
                applicationInfoSource = applicationInfoSource,
            )
        }.flow
    }

    override fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<AppUsageInfo>>>> {
        return Pager(
            PagingConfig(pageSize = Constants.FIRST_COLLECT_DAY.toInt())
        ) {
            GetDayPagingAppUsageInfo(
                appUsageDao = appUsageDao,
                targetDate = date,
                packageName = packageName
            )
        }.flow
    }
}