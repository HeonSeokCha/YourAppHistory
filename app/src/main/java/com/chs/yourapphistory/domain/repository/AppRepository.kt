package com.chs.yourapphistory.domain.repository

import android.graphics.Bitmap
import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppDetailInfo
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

    suspend fun getPagingAppUsedInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>

    suspend fun getPagingAppForegroundInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>


    suspend fun getPagingAppLaunchInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>


    suspend fun getPagingAppNotifyInfo(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>

    suspend fun getAppIconMap(): HashMap<String, Bitmap?>
}