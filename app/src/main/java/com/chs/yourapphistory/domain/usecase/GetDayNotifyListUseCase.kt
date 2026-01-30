package com.chs.yourapphistory.domain.usecase

import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.repository.AppRepository
import org.koin.core.annotation.Single

@Single
class GetDayNotifyListUseCase (
    private val repository: AppRepository
) {
    suspend operator fun invoke(targetDate: Long): List<Pair<AppInfo, Int>> {
        return repository.getDayNotifyAppList(targetDate)
    }
}