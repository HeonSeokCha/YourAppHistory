package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class GetDayUseAppListUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(date: LocalDate): Flow<Resource<List<Pair<AppInfo, String>>>> {
        return flow {
            emit(Resource.Loading)
            repository.getDayUsedAppInfoList(date).collect {
                if (it.isEmpty()) {
                    emit(Resource.Loading)
                } else {
                    val a = it.map {
                        it.first to (
                            it.second.map {
                                if (date.dayOfMonth < it.endUseTime.toLocalDate().dayOfMonth) {
                                    val nextDayStartMilli =
                                        date.plusDays(1L).atStartOfDayToMillis()
                                    return@map (nextDayStartMilli - it.beginUseTime.toMillis())
                                }

                                if (date.dayOfMonth > it.beginUseTime.toLocalDate().dayOfMonth) {
                                    val dayStartMilli = date.atStartOfDayToMillis()
                                    return@map (it.endUseTime.toMillis() - dayStartMilli)
                                }

                                (it.endUseTime.toMillis() - it.beginUseTime.toMillis())
                            }.sum()
                        )
                    }

                    emit(
                        Resource.Success(
                            a.sortedWith(compareByDescending { it.second }).map {
                                it.first to it.second.convertToRealUsageTime()
                            }
                        )
                    )
                }
            }
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }
}