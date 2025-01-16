package com.chs.yourapphistory.domain.usecase import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetPagingLaunchListUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        return repository.getDayLaunchAppList()
    }
}