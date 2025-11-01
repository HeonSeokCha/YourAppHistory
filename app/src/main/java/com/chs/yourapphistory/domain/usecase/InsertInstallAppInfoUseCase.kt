package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.repository.AppRepository
import org.koin.core.annotation.Single

@Single
class InsertInstallAppInfoUseCase (
    private val repository: AppRepository
) {
    suspend operator fun invoke() {
        repository.insertInstallAppInfo()
    }
}