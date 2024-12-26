package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetDayPagingNotifyUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        return repository.getDayNotifyAppList()
    }
}