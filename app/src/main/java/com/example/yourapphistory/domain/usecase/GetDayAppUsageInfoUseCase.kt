package com.example.yourapphistory.domain.usecase

import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import com.example.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class GetDayAppUsageInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<Pair<AppInfo, List<AppUsageInfo>>>> {
        return repository.getAppUsageInfo(date)
    }
}