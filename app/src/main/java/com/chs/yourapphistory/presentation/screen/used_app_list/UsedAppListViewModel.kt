package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.domain.usecase.GetAppIconMapUseCase
import com.chs.yourapphistory.domain.usecase.GetDayPagingForegroundUsedUseCase
import com.chs.yourapphistory.domain.usecase.GetDayPagingLaunchUseCase
import com.chs.yourapphistory.domain.usecase.GetDayPagingNotifyUseCase
import com.chs.yourapphistory.domain.usecase.GetDayPagingUsedUseCase
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            getApplicationsInfo()
            changeSortOption(state.sortOption)
        }
    }

    private suspend fun getApplicationsInfo() {
        withContext(Dispatchers.IO) {
            awaitAll(
                async { getInstallAppInfoUseCase() },
                async { insertAppUsageInfoUseCase() },
                async { state = state.copy(appIconList = getAppIconMapUseCase()) }
            )
        }
    }

    fun changeSortOption(option: UsedAppEvent) {
        state = when (option) {
            UsedAppEvent.RefreshAppUsageInfo -> {
                viewModelScope.launch {
                    getApplicationsInfo()
                    changeSortOption(state.sortOption)
                }
                state.copy(
                    appInfoList = null,
                    appIconList = hashMapOf()
                )
            }

            UsedAppEvent.GetUsageEvent.AppForegroundUsageEvent -> {
                state.copy(
                    appInfoList = getDayPagingForegroundUsedUseCase().cachedIn(viewModelScope)
                )
            }
            UsedAppEvent.GetUsageEvent.AppLaunchEvent -> {
                state.copy(
                    appInfoList = getDayPagingLaunchUseCase().cachedIn(viewModelScope)
                )
            }
            UsedAppEvent.GetUsageEvent.AppNotifyEvent -> {
                state.copy(
                    appInfoList = getDayPagingNotifyUseCase().cachedIn(viewModelScope)
                )
            }
            UsedAppEvent.GetUsageEvent.AppUsageEvent -> {
                state.copy(
                    appInfoList = getDayPagingUsedUseCase().cachedIn(viewModelScope)
                )
            }
        }
    }
}