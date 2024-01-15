package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.model.AppNotifyInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class GetAppNotifyCountUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(
        date: LocalDate,
        packageName: String
    ): List<Pair<Int, Long>> {
        return calculateTimezoneNotifyCount(
            list = repository.getAppNotifyInfoList(
                date = date,
                packageName = packageName
            )
        )
    }

    private suspend fun calculateTimezoneNotifyCount(list: List<AppNotifyInfo>): List<Pair<Int, Long>> {
        val usageMap = object : HashMap<Int, Long>() {
            init {
                for (i in 0..23) {
                    put(i, 0L)
                }
            }
        }

        withContext(Dispatchers.Default) {
            list.forEach { appUsageInfo ->
                usageMap.computeIfPresent(appUsageInfo.notifyTime.hour) { _, value ->
                    value + 1
                }
            }
        }
        return usageMap.toList()
    }
}