package com.chs.yourapphistory.domain.repository

import android.graphics.Bitmap
import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppUsageInfo()

    suspend fun insertInstallAppInfo()

    suspend fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, Int>>>>>

    suspend fun getDayForegroundUsedAppList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, Int>>>>>

    suspend fun getDayNotifyAppList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>>

    suspend fun getDayLaunchAppList():  Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>>

    suspend fun getDailyPagingAppUsedInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>

    suspend fun getDailyPagingAppForegroundInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>


    suspend fun getDailyPagingAppLaunchInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>

    suspend fun getDailyPagingAppNotifyInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>

    suspend fun getWeeklyPagingAppUsedInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>>

    suspend fun getWeeklyPagingAppForegroundInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>>

    suspend fun getWeeklyPagingAppLaunchInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>>

    suspend fun getWeeklyPagingAppNotifyInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>>

    suspend fun getAppIconMap(): HashMap<String, Bitmap?>

    suspend fun getMinDate(): LocalDate

    suspend fun deleteUsageInfo(packageName: String)
}