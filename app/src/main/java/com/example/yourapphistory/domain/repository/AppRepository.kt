package com.example.yourapphistory.domain.repository

import com.example.yourapphistory.common.Resource
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppUsageInfo()

    suspend fun insertInstallAppInfo()

    suspend fun getDayUsedAppInfoList(date: LocalDate): Flow<Resource<List<Pair<AppInfo, String>>>>

    suspend fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): Flow<Resource<List<AppUsageInfo>>>

    suspend fun getOldestAppUsageCollectDay(): LocalDate

}