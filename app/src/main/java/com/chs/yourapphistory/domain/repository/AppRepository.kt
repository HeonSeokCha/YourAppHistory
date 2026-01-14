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

    fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, Int>>>>>

    fun getDayForegroundUsedAppList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, Int>>>>>

    fun getDayNotifyAppList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>>

    fun getDayLaunchAppList():  Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>>

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