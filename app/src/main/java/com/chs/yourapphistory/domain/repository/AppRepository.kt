package com.chs.yourapphistory.domain.repository

import android.graphics.Bitmap
import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppUsageInfo()

    suspend fun insertInstallAppInfo()

    fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, Int>>>>>

    fun getDayForegroundUsedAppList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, Int>>>>>

    fun getDayNotifyAppList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>>

    fun getDayLaunchAppList():  Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>>

    fun getDailyPagingAppUsedInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>

    fun getDailyPagingAppForegroundInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>


    fun getDailyPagingAppLaunchInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>

    fun getDailyPagingAppNotifyInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>

    fun getWeeklyPagingAppUsedInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>>

    fun getWeeklyPagingAppForegroundInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>>

    fun getWeeklyPagingAppLaunchInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>>

    fun getWeeklyPagingAppNotifyInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>>

    suspend fun getAppIconMap(): HashMap<String, Bitmap?>

    suspend fun getMinDate(): LocalDate

    suspend fun deleteUsageInfo(packageName: String)
}