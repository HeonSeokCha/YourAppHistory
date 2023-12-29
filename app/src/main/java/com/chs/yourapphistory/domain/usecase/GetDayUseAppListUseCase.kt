package com.chs.yourapphistory.domain.usecase

import android.util.Log
import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
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
                    val a = withContext(Dispatchers.Default){
                        it.map {
                            it.first to (
                                    it.second.sumOf {
                                        if (date.dayOfMonth < it.endUseTime.dayOfMonth) {
                                            date.plusDays(1L)
                                                .atStartOfDayToMillis() - it.beginUseTime.toMillis()
                                        }

                                        if (date.dayOfMonth > it.beginUseTime.dayOfMonth) {
                                            it.endUseTime.toMillis() - date.atStartOfDayToMillis()
                                        }

                                        (it.endUseTime.toMillis() - it.beginUseTime.toMillis())
                                    }
                                    )
                        }
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
