package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetDayPagingUseAppListUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, String>>>>> {
        return repository.getDayPagingUsedAppInfo().map { pagingData ->
            pagingData.map {
                val date = it.first
                date to it.second.map {
                        it.key to (
                            it.value.map {
                                if (date.dayOfMonth < it.endUseTime.toLocalDate().dayOfMonth) {
                                    val nextDayStartMilli = date.plusDays(1L).atStartOfDayToMillis()
                                    return@map (nextDayStartMilli - it.beginUseTime.toMillis())
                                }

                                if (date.dayOfMonth > it.beginUseTime.toLocalDate().dayOfMonth) {
                                    val dayStartMilli = date.atStartOfDayToMillis()
                                    return@map (it.endUseTime.toMillis() - dayStartMilli)
                                }

                                (it.endUseTime.toMillis() - it.beginUseTime.toMillis())
                            }.sum()
                        )
                }.sortedByDescending { it.second }.map {
                    it.first to it.second.convertToRealUsageTime()
                }
            }
        }
    }
}