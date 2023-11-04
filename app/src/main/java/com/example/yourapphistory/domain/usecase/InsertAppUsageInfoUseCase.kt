package com.example.yourapphistory.domain.usecase

import com.example.yourapphistory.domain.repository.AppRepository
import javax.inject.Inject

class InsertAppUsageInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke() {
        repository.insertAppUsageInfo()
    }
}