package com.chs.yourapphistory.domain.repository

import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppUsageInfo()

    suspend fun insertInstallAppInfo()

    fun getDayUsedAppInfoList(date: LocalDate): Flow<Resource<List<Pair<AppInfo, String>>>>

    fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): Flow<Resource<List<AppUsageInfo>>>

    suspend fun getOldestAppUsageCollectDay(): LocalDate

}