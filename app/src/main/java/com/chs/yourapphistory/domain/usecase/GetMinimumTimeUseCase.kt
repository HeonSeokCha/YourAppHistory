package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.repository.AppRepository
import org.koin.core.annotation.Single
import java.time.LocalDate

@Single
class GetMinimumTimeUseCase(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): LocalDate {
        return repository.getMinDate()
    }
}