package com.chs.yourapphistory.domain.repository

import androidx.paging.PagingData
import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppUsageInfo()

    suspend fun insertInstallAppInfo()

    fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, List<AppUsageInfo>>>>>>

    fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<AppUsageInfo>>>>

    suspend fun getOldestAppUsageCollectDay(): LocalDate

}