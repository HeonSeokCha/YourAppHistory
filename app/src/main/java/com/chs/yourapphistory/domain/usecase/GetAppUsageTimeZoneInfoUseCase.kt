package com.chs.yourapphistory.domain.usecase

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppUsageInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetAppUsageTimeZoneInfoUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(
        date: LocalDate,
        packageName: String
    ): Flow<PagingData<Pair<LocalDate, List<AppUsageInfo>>>> {
        return repository.getAppUsageInfoList(
            date = date,
            packageName = packageName
        )
    }
}