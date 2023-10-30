package com.example.yourapphistory.data.repository

import com.example.yourapphistory.data.ApplicationInfoSource
import com.example.yourapphistory.data.db.dao.AppUsageDao
import com.example.yourapphistory.data.db.dao.AppUsageEventDao
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import com.example.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val applicationInfoSource: ApplicationInfoSource,
    private val appUsageDao: AppUsageDao,
    private val appUsageEventDao: AppUsageEventDao
) : AppRepository {

    override fun getAppUsageInfo(date: LocalDate): Flow<Map<AppInfo, List<AppUsageInfo>>> {
        TODO("Not yet implemented")
    }
}