package com.example.yourapphistory.domain.repository

import com.example.yourapphistory.common.Resource
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppInfo(list: List<AppInfo>)

    suspend fun insertAppUsageInfo()

    suspend fun getDayUsedAppInfoList(): List<AppInfo>

    suspend fun getAppUsageInfo(date: LocalDate): Flow<Resource<List<Pair<AppInfo, List<AppUsageInfo>>>>>

    suspend fun getOldestAppUsageCollectDay(): LocalDate

}