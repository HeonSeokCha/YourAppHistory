package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.model.AppForegroundUsageInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import java.time.LocalDate
import javax.inject.Inject

class GetAppForegroundUsageInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(
        targetDate: LocalDate,
        packageName: String
    ): List<AppForegroundUsageInfo> {
        return repository.getAppForegroundUsageInfoList(
            date = targetDate,
            packageName = packageName
        )
    }
}