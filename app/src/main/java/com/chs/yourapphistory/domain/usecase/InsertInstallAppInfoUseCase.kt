package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.repository.AppRepository
import javax.inject.Inject

class InsertInstallAppInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke() {
        repository.insertInstallAppInfo()
    }
}