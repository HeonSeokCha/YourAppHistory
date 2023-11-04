package com.example.yourapphistory.data.repository

import android.util.Log
import com.example.yourapphistory.common.Constants
import com.example.yourapphistory.common.isZero
import com.example.yourapphistory.common.toLocalDate
import com.example.yourapphistory.common.toLocalDateTime
import com.example.yourapphistory.common.toMillis
import com.example.yourapphistory.data.ApplicationInfoSource
import com.example.yourapphistory.data.db.dao.AppUsageDao
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import com.example.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val applicationInfoSource: ApplicationInfoSource,
    private val appUsageDao: AppUsageDao
) : AppRepository {

    override suspend fun insertAppUsageInfo() {
        appUsageDao.insert(
            *applicationInfoSource.getAppUsageInfoList(
                applicationInfoSource.getUsageEvent(getLastUsageEventTime())
            ).toTypedArray()
        )
    }

    private suspend fun getLastUsageEventTime(): Long {
        val localEndUseTime: Long = appUsageDao.getLastEndUseTime()?.endUseTime ?: 0L
        return if (localEndUseTime == 0L) {
            LocalDate.now().minusDays(Constants.FIRST_COLLECT_DAY).toMillis()
        } else {
            localEndUseTime
        }
    }

    override fun getAppUsageInfo(date: LocalDate): Flow<List<Pair<AppInfo, List<AppUsageInfo>>>> {
        val a = appUsageDao.getDayUsageInfoList(date.toMillis())
//            .filter {
//                it.any {
//                    applicationInfoSource.getInstalledLauncherPackageNameList().contains(it.packageName)
//                }
//            }
            .map { appUsageList ->
                Log.e("TEST", LocalDateTime.now().toString())
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
        Log.e("TEST", LocalDateTime.now().toString())
        return a
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