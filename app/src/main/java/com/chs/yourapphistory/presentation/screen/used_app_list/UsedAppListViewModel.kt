package com.chs.yourapphistory.presentation.screen.used_app_list

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val _state = MutableStateFlow(UsedAppListState())
    val state = _state
        .onStart {
            getApplicationsInfo()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            _state.value
        )

    private suspend fun getApplicationsInfo() {
        withContext(Dispatchers.IO) {
            awaitAll(
                async { getInstallAppInfoUseCase() },
                async { insertAppUsageInfoUseCase() },
                async { _state.update { it.copy(appIconList = getAppIconMapUseCase()) } }
            )
        }

        getEventList(_state.value.sortOption)
    }

    fun changeEvent(option: UsedAppEvent) {
        when (option) {
            UsedAppEvent.RefreshAppUsageInfo -> {
                viewModelScope.launch {
                    getApplicationsInfo()
                }
            }

            is UsedAppEvent.GetUsageEvent -> {
                getEventList(option)
            }

            else -> Unit
        }
    }

    private fun getEventList(option: UsedAppEvent.GetUsageEvent) {
        viewModelScope.launch {
            when (option) {
                is UsedAppEvent.GetUsageEvent.AppForegroundUsageEvent -> {
                    _state.update {
                        it.copy(
                            appInfoList = getDayPagingForegroundUsedUseCase().cachedIn(viewModelScope),
                            sortOption = option
                        )
                    }
                }

                is UsedAppEvent.GetUsageEvent.AppLaunchEvent -> {
                    _state.update {
                        it.copy(
                            appInfoList = getDayPagingLaunchUseCase().cachedIn(viewModelScope),
                            sortOption = option
                        )
                    }
                }

                is UsedAppEvent.GetUsageEvent.AppNotifyEvent -> {
                    _state.update {
                        it.copy(
                            appInfoList = getDayPagingNotifyUseCase().cachedIn(viewModelScope),
                            sortOption = option
                        )
                    }
                }

                is UsedAppEvent.GetUsageEvent.AppUsageEvent -> {
                    _state.update {
                        it.copy(
                            appInfoList = getDayPagingUsedUseCase().cachedIn(viewModelScope),
                            sortOption = option
                        )
                    }
                }
            }
        }
    }
}