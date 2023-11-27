package com.example.yourapphistory.domain.usecase

import com.example.yourapphistory.common.Resource
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDayUseAppListUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(date: LocalDate): Flow<Resource<List<Pair<AppInfo, String>>>> {
        return repository.getDayUsedAppInfoList(date)
    }
}