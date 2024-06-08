package com.chs.yourapphistory.domain.repository

import android.graphics.Bitmap
import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppDetailInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppNotifyInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    suspend fun insertAppUsageInfo()

    suspend fun insertInstallAppInfo()

    fun getDayUsedAppInfoList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, Int>>>>>

    fun getDayForegroundUsedAppList(): Flow<PagingData<Pair<LocalDate,List<Pair<AppInfo, Int>>>>>

    fun getDayNotifyAppList(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>>

    fun getPagingAppDetailInfo(): Flow<PagingData<Pair<LocalDate, List<AppDetailInfo>>>>

    suspend fun getPackageLabel(packageName: String): String

    suspend fun getAppIconMap(): HashMap<String, Bitmap?>
}