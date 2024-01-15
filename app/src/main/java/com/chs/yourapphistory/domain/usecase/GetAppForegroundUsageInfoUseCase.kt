package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.common.calculateTimeZoneUsage
import com.chs.yourapphistory.domain.repository.AppRepository
import java.time.LocalDate
import javax.inject.Inject

class GetAppForegroundUsageInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(
        date: LocalDate,
        packageName: String
    ): List<Pair<Int, Long>> {
        return calculateTimeZoneUsage(
            date = date,
            list = repository.getAppForegroundUsageInfoList(
                date = date,
                packageName = packageName
            )
        )
    }
}