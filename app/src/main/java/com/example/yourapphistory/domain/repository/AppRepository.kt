package com.example.yourapphistory.domain.repository

import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    fun getAppUsageInfo(date: LocalDate): Flow<Map<AppInfo, List<AppUsageInfo>>>

}