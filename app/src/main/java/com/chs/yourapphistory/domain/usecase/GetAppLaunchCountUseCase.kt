package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.repository.AppRepository
import java.time.LocalDate
import javax.inject.Inject

class GetAppLaunchCountUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(
        date: LocalDate,
        packageName: String
    ): Int {
        return repository.getAppUsageInfoList(
            date = date,
            packageName
        ).size
    }
}