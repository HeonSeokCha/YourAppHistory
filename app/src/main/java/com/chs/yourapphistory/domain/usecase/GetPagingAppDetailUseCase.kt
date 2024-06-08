package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.repository.AppRepository
import java.time.LocalDate
import javax.inject.Inject

class GetPagingAppDetailUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(targetDate: LocalDate) {

    }
}