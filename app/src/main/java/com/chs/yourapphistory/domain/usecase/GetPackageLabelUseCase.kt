package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.repository.AppRepository
import javax.inject.Inject

class GetPackageLabelUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(packageName: String): String {
        return repository.getPackageLabel(packageName)
    }
}