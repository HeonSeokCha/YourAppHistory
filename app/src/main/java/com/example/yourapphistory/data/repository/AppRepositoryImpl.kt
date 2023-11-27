package com.example.yourapphistory.data.repository

import com.example.yourapphistory.common.Constants
import com.example.yourapphistory.common.Resource
import com.example.yourapphistory.common.atEndOfDayToMillis
import com.example.yourapphistory.common.atStartOfDayToMillis
import com.example.yourapphistory.common.convertToRealUsageTime
import com.example.yourapphistory.common.isZero
import com.example.yourapphistory.common.toLocalDate
import com.example.yourapphistory.common.toLocalDateTime
import com.example.yourapphistory.common.toMillis
import com.example.yourapphistory.data.ApplicationInfoSource
import com.example.yourapphistory.data.db.dao.AppInfoDao
import com.example.yourapphistory.data.db.dao.AppUsageDao
import com.example.yourapphistory.data.db.entity.AppInfoEntity
import com.example.yourapphistory.data.toAppInfo
import com.example.yourapphistory.data.toAppUsageInfo
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import com.example.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
        val launcherList: List<String> = applicationInfoSource.getInstalledLauncherPackageNameList()
        appInfoDao.insert(
            *launcherList.filterNot { launcherPackageName ->
                localList.any { it == launcherPackageName }
            }.map { packageName ->
                AppInfoEntity(
                    packageName = packageName,
                    label = applicationInfoSource.getApplicationLabel(packageName),
                    icon = applicationInfoSource.getApplicationIcon(packageName)
                )
            }.toTypedArray()
        )
    }

    override suspend fun insertAppUsageInfo() {
        val rawEvents = applicationInfoSource.getUsageEvent(
            getLastUsageEventTime()
        )

        appUsageDao.insert(
            *applicationInfoSource.getAppUsageInfoList(rawEvents).toTypedArray()
        )
    }

    override suspend fun getDayUsedAppInfoList(
        date: LocalDate
    ): Flow<Resource<List<Pair<AppInfo, String>>>> {
        return flow {
            emit(Resource.Loading())
            val result = appInfoDao.getDayUsedAppInfoList(
                    beginTime = date.atStartOfDayToMillis(),
                    endTime = date.atEndOfDayToMillis()
                ).map {
                    it.key.toAppInfo() to it.value.convertToRealUsageTime()
                }

            emit(Resource.Success(result))
        }
    }

    override suspend fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): Flow<Resource<List<AppUsageInfo>>> {
        return flow {
            emit(Resource.Loading())
            val result = appUsageDao.getUsageInfoList(
                beginTime = date.atStartOfDayToMillis(),
                endTime = date.atEndOfDayToMillis(),
                packageName = packageName
            ).map {
                it.toAppUsageInfo()
            }
            emit(Resource.Success(result))
        }
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