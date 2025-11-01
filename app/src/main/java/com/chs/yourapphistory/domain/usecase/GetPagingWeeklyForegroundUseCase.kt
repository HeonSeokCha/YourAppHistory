package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import java.time.LocalDate

@Single
class GetPagingWeeklyForegroundUseCase (
    private val repository: AppRepository
) {
    operator fun invoke(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>> {
        return repository.getWeeklyPagingAppForegroundInfo(
            packageName = packageName,
            targetDate = targetDate
        )
    }
}