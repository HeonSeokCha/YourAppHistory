package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import java.time.LocalDate

@Single
class GetPagingDailyNotifyUseCase(
    private val repository: AppRepository
) {
    operator fun invoke(
        targetDate: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>> {
        return repository.getDailyPagingAppNotifyInfo(
            targetDate = targetDate,
            packageName = packageName
        )
    }
}