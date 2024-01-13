package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.model.AppNotifyInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import java.time.LocalDate
import javax.inject.Inject

class GetAppNotifyCountUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(
        packageName: String,
        targetDate: LocalDate
    ): List<AppNotifyInfo> {
        return repository.getAppNotifyInfoList(
            date = targetDate,
            packageName = packageName
        )
    }
}