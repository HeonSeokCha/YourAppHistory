package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.domain.model.AppUsageInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDayAppUsageInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(
        date: LocalDate,
        packageName: String
    ): Flow<Resource<List<AppUsageInfo>>> {
        return repository.getAppUsageInfoList(
            date = date,
            packageName = packageName
        )
    }
}