package com.example.yourapphistory.domain.usecase

import com.example.yourapphistory.common.Resource
import com.example.yourapphistory.domain.model.AppUsageInfo
import com.example.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDayAppUsageInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(
        date: LocalDate,
        packageName: String
    ): Flow<Resource<List<AppUsageInfo>>> {
        return repository.getAppUsageInfoList(
            date = date,
            packageName = packageName
        )
    }
}