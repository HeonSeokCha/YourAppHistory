package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDayUseAppListUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(date: LocalDate): Flow<Resource<List<Pair<AppInfo, String>>>> {
        return repository.getDayUsedAppInfoList(date)
    }
}