package com.chs.yourapphistory.domain.usecase

import android.graphics.Bitmap
import com.chs.yourapphistory.domain.repository.AppRepository
import javax.inject.Inject

class GetAppIconMapUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): HashMap<String, Bitmap?> {
        return repository.getAppIconMap()
    }
}