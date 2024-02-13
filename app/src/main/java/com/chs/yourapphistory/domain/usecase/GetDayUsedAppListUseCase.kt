package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.getDayOfMonth
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class GetDayUsedAppListUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, String>>>>> {

        return repository.getDayUsedAppInfoList().map { pagingData ->
            pagingData.map {
                it.first to withContext(Dispatchers.Default) {
                    val date = it.first
                    it.second.map {
                        val totalTime = it.second.sumOf {
                            if (date.dayOfMonth < it.second.getDayOfMonth()) {
                                date.plusDays(1L)
                                    .atStartOfDayToMillis() - it.first
                            }

                            if (date.dayOfMonth > it.first.getDayOfMonth()) {
                                it.second - date.atStartOfDayToMillis()
                            }

                            (it.second - it.first)
                        }
                        it.first to totalTime
                    }.map {
                        it.first to it.second.convertToRealUsageTime()
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}
