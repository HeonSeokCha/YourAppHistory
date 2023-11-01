package com.example.yourapphistory.data.repository

import com.example.yourapphistory.common.Constants
import com.example.yourapphistory.common.toLocalDate
import com.example.yourapphistory.common.toLocalDateTime
import com.example.yourapphistory.common.toMillis
import com.example.yourapphistory.data.ApplicationInfoSource
import com.example.yourapphistory.data.db.dao.AppUsageDao
import com.example.yourapphistory.data.db.dao.AppUsageEventDao
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import com.example.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val applicationInfoSource: ApplicationInfoSource,
    private val appUsageEventDao: AppUsageEventDao,
    private val appUsageDao: AppUsageDao
) : AppRepository {

    override suspend fun insertAppUsageInfo() {
        applicationInfoSource.insertUsageEvent(getLastUsageEventTime())
    }

    private suspend fun getLastUsageEventTime(): Long {
        val localEndUseTime: Long = appUsageDao.getLastEndUseTime()
        return if (localEndUseTime == 0L) {
            LocalDate.now().minusDays(Constants.FIRST_COLLECT_DAY).toMillis()
        } else {
            localEndUseTime
        }
    }

    override fun getAppUsageInfo(date: LocalDate): Flow<List<Pair<AppInfo, List<AppUsageInfo>>>> {
        return appUsageDao.getDayUsageInfoList(date.toMillis()).map { appUsageList ->
            appUsageList.groupBy { it.packageName }.map {
                AppInfo(
                    packageName = it.key,
                    label = applicationInfoSource.getApplicationLabel(it.key),
                    icon = applicationInfoSource.getApplicationIcon(it.key)
                ) to it.value.map { appUSageEntity ->
                    AppUsageInfo(
                        packageName = appUSageEntity.packageName,
                        beginUseTime = appUSageEntity.beginUseTime.toLocalDateTime(),
                        endUseTime = appUSageEntity.endUseTime.toLocalDateTime()
                    )
                }
            }.sortedByDescending {
                it.second.sumOf { appUSageInfo ->
                    appUSageInfo.endUseTime.toMillis() - appUSageInfo.beginUseTime.toMillis()
                }
            }
        }
    }

    override suspend fun getOldestAppUsageCollectDay(): LocalDate {
        return appUsageDao.getOldestCollectTime().toLocalDate()
    }
}