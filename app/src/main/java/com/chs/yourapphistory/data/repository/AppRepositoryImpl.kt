package com.chs.yourapphistory.data.repository

import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.common.atEndOfDayToMillis
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.convertToRealUsageTime
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
import kotlinx.coroutines.flow.catch
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

    override fun getDayUsedAppInfoList(
        date: LocalDate
    ): Flow<Resource<List<Pair<AppInfo, String>>>> {
        return flow {
            emit(Resource.Loading)
            appInfoDao.getDayUsedAppInfoList(
                beginTime = date.atStartOfDayToMillis(),
                endTime = date.atEndOfDayToMillis()
            ).collect {
                if (it.isEmpty()) {
                    emit(Resource.Loading)
                } else {
                    emit(
                        Resource.Success(
                            it.map {
                                it.key.toAppInfo() to it.value.convertToRealUsageTime()
                            }
                        )
                    )
                }
            }
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): Flow<Resource<List<AppUsageInfo>>> {
        return flow {
            emit(Resource.Loading)
            appUsageDao.getUsageInfoList(
                beginTime = date.atStartOfDayToMillis(),
                endTime = date.atEndOfDayToMillis(),
                packageName = packageName
            ).collect {
                emit(
                    Resource.Success(
                        it.map { it.toAppUsageInfo() }
                    )
                )
            }
        }.catch {
            emit(Resource.Error(it.message.toString()))
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