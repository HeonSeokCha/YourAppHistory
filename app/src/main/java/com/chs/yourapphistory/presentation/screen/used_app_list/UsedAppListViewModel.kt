package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.work.WorkManager
import com.chs.yourapphistory.data.model.UsageEventType
import com.chs.yourapphistory.domain.usecase.GetAppIconMapUseCase
import com.chs.yourapphistory.domain.usecase.GetDayPagingForegroundUsedUseCase
import com.chs.yourapphistory.domain.usecase.GetDayPagingLaunchUseCase
import com.chs.yourapphistory.domain.usecase.GetDayPagingNotifyUseCase
import com.chs.yourapphistory.domain.usecase.GetDayPagingUsedUseCase
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsedAppListViewModel @Inject constructor(
    private val getDayPagingUsedUseCase: GetDayPagingUsedUseCase,
    private val getDayPagingForegroundUsedUseCase: GetDayPagingForegroundUsedUseCase,
    private val getDayPagingNotifyUseCase: GetDayPagingNotifyUseCase,
    private val getDayPagingLaunchUseCase: GetDayPagingLaunchUseCase,
    private val getAppIconMapUseCase: GetAppIconMapUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase,
    private val getInstallAppInfoUseCase: InsertInstallAppInfoUseCase
) : ViewModel() {

    var state by mutableStateOf(UsedAppListState())
        private set

    init {
        viewModelScope.launch {
            awaitAll(
                async { getInstallAppInfoUseCase() },
                async { insertAppUsageInfoUseCase() },
                async {
                    state = state.copy(appIconList = getAppIconMapUseCase())
                }
            )

            changeSortOption(state.sortOption)
        }
    }


    fun changeSortOption(option: UsageEventType) {
        state = when (option) {
            UsageEventType.AppUsageEvent -> {
                state.copy(
                    appInfoList = getDayPagingUsedUseCase().cachedIn(viewModelScope),
                    sortOption = option
                )
            }

            UsageEventType.AppForegroundUsageEvent -> {
                state.copy(
                    appInfoList = getDayPagingForegroundUsedUseCase().cachedIn(viewModelScope),
                    sortOption = option
                )
            }

            UsageEventType.AppNotifyEvent -> {
                state.copy(
                    appInfoList = getDayPagingNotifyUseCase().cachedIn(viewModelScope),
                    sortOption = option
                )
            }

            UsageEventType.AppLaunchEvent -> {
                state.copy(
                    appInfoList = getDayPagingLaunchUseCase().cachedIn(viewModelScope),
                    sortOption = option
                )
            }
        }
    }
}