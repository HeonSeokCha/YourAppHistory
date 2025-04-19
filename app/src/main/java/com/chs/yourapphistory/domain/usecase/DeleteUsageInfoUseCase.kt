package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.repository.AppRepository
import jakarta.inject.Inject

class DeleteUsageInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(packageName: String) {
        repository.deleteUsageInfo(packageName)
    }
}