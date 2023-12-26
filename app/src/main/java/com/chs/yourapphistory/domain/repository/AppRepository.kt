package com.chs.yourapphistory.domain.repository

import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppUsageInfo()

    suspend fun insertInstallAppInfo()

    fun getDayUsedAppInfoList(date: LocalDate): Flow<List<Pair<AppInfo, List<AppUsageInfo>>>>

    suspend fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): List<AppUsageInfo>

    suspend fun getOldestAppUsageCollectDay(): LocalDate

}