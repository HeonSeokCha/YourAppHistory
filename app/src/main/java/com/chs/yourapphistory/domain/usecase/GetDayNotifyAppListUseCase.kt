package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetDayNotifyAppListUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, String>>>>> {
        return repository.getDayNotifyAppList().map { pagingData ->
            pagingData.map {
                it.first to it.second.map {
                    it.first to "${it.second}회"
                }
            }
        }
    }
}