package com.chs.yourapphistory.domain.repository

import android.graphics.Bitmap
import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppBaseUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppNotifyInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppUsageInfo()

    suspend fun insertInstallAppInfo()

    fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, List<AppBaseUsageInfo.AppUsageInfo>>>>>>

    suspend fun getAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ): List<AppBaseUsageInfo.AppUsageInfo>

    suspend fun getAppForegroundUsageInfoList(
        date: LocalDate,
        packageName: String
    ): List<AppBaseUsageInfo.AppForegroundUsageInfo>

    suspend fun getAppNotifyInfoList(
        date: LocalDate,
        packageName: String
    ): List<AppNotifyInfo>

    suspend fun getOldestAppUsageCollectDay(): LocalDate

    suspend fun getPackageLabel(packageName: String): String

    suspend fun getAppIconMap(): HashMap<String, Bitmap?>
}