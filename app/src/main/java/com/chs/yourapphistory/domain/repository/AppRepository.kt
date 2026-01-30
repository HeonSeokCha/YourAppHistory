package com.chs.yourapphistory.domain.repository

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import com.chs.yourapphistory.domain.model.SortType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppUsageInfo()

    suspend fun insertInstallAppInfo()

    suspend fun getDayUsedAppInfoList(targetDateMilli: Long): List<Pair<AppInfo, Int>>

    suspend fun getDayForegroundUsedAppList(targetDateMilli: Long): List<Pair<AppInfo, Int>>

    suspend fun getDayNotifyAppList(targetDateMilli: Long): List<Pair<AppInfo, Int>>

    suspend fun getDayLaunchAppList(targetDateMilli: Long): List<Pair<AppInfo, Int>>

    fun getWeeklyPagingAppInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Map<SortType, List<Pair<LocalDate, Int>>>>>

    fun getDailyPagingAppInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Map<SortType, List<Pair<Int, Int>>>>>

    fun getWeeklyPagingTotalAppInfo(): Flow<PagingData<Map<SortType, List<Pair<LocalDate, List<AppTotalUsageInfo>>>>>>

    suspend fun getMinDate(): LocalDate

    suspend fun deleteUsageInfo(packageName: String)
}