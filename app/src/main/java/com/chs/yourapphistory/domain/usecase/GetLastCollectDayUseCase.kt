package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.repository.AppRepository
import java.time.LocalDate
import javax.inject.Inject

class GetLastCollectDayUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): LocalDate {
        return repository.getOldestAppUsageCollectDay()
    }
}
