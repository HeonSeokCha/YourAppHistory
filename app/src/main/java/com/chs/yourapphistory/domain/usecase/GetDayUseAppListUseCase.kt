package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class GetDayUseAppListUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, String>>>>> {
        return repository.getDayUsedAppInfoList().map {
            it.map {
                val a = withContext(Dispatchers.Default) {
                    val date = it.first
                    it.second.map {
                        val totalTime = it.second.sumOf {
                            if (date.dayOfMonth < it.endUseTime.dayOfMonth) {
                                date.plusDays(1L)
                                    .atStartOfDayToMillis() - it.beginUseTime.toMillis()
                            }

                            if (date.dayOfMonth > it.beginUseTime.dayOfMonth) {
                                it.endUseTime.toMillis() - date.atStartOfDayToMillis()
                            }

                            (it.endUseTime.toMillis() - it.beginUseTime.toMillis())
                        }
                        it.first to totalTime
                    }.sortedByDescending { it.second }.map {
                        it.first to it.second.convertToRealUsageTime()
                    }
                }
                it.first to a
            }
        }
    }
}
