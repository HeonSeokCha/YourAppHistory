package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetPagingAppLaunchUseCase @Inject constructor(
    private val repository: AppRepository
) {

    suspend operator fun invoke(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        return repository.getDailyPagingAppLaunchInfo(
            targetDate = targetDate,
            packageName = packageName
        )
    }
}