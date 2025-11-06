package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import java.time.LocalDate

@Single
class GetPagingWeeklyUseCase(
    private val repository: AppRepository
) {
    operator fun invoke(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Map<SortType, List<Pair<LocalDate, Int>>>>> {
        return repository.getWeeklyPagingAppInfo(
            targetDate = targetDate,
            packageName = packageName
        )
    }
}