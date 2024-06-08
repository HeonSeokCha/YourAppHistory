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

class GetDayPagingUsedUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        return repository.getDayUsedAppInfoList()
    }
}
