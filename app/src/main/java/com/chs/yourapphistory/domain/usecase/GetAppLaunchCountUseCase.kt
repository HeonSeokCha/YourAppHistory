package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.model.AppBaseUsageInfo.AppUsageInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class GetAppLaunchCountUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(
        date: LocalDate,
        packageName: String
    ): List<Pair<Int, Long>> {
        return calculateTimezoneLaunchCount(repository.getAppUsageInfoList(
            date = date,
            packageName
        ))
    }

    private suspend fun calculateTimezoneLaunchCount(list: List<AppUsageInfo>): List<Pair<Int, Long>> {
        val usageMap = object : HashMap<Int, Long>() {
            init {
                for (i in 0..23) {
                    put(i, 0L)
                }
            }
        }

        withContext(Dispatchers.Default) {
            list.forEach { appUsageInfo ->
                usageMap.computeIfPresent(appUsageInfo.beginUseTime.hour) { _, value ->
                    value + 1
                }
            }
        }
        return usageMap.toList()
    }
}