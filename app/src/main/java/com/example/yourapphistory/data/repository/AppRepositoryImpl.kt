package com.example.yourapphistory.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.example.yourapphistory.common.Constants
import com.example.yourapphistory.common.Resource
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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    override suspend fun getAppUsageInfo(
        date: LocalDate
    ): Flow<Resource<List<Pair<AppInfo, List<AppUsageInfo>>>>> {
        return withContext(Dispatchers.Default) {
            flow {
                emit(Resource.Loading())
                val a = appUsageDao.getDayUsageInfoList(date.toMillis())
                val b = a.map {
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
                emit(Resource.Success(b))
            }
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