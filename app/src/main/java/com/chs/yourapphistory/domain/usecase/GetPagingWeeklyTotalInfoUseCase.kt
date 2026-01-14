package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import java.time.LocalDate

@Single
class GetPagingWeeklyTotalInfoUseCase(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<PagingData<Map<SortType, List<Pair<LocalDate, List<AppTotalUsageInfo>>>>>> {
        return repository.getWeeklyPagingTotalAppInfo()
    }
}