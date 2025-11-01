package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import java.time.LocalDate

@Single
class GetPagingForegroundListUseCase (
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        return repository.getDayForegroundUsedAppList()
    }
}
