package com.chs.yourapphistory.domain.usecase

import android.graphics.Bitmap
import com.chs.yourapphistory.domain.repository.AppRepository
import org.koin.core.annotation.Single

@Single
class GetAppIconMapUseCase(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): HashMap<String, Bitmap?> {
        return repository.getAppIconMap()
    }
}