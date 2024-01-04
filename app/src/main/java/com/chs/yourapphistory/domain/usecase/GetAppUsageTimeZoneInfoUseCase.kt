package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.common.calculateTimeZoneUsage
import com.chs.yourapphistory.domain.repository.AppRepository
import java.time.LocalDate
import javax.inject.Inject

class GetAppUsageTimeZoneInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(
        date: LocalDate,
        packageName: String
    ): List<Pair<Int, Long>>{
        return calculateTimeZoneUsage(
            date = date,
            list = repository.getAppUsageInfoList(
                date = date,
                packageName = packageName
            )
        )
    }
}