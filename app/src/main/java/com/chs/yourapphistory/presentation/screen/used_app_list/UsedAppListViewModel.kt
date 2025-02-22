package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.domain.usecase.GetAppIconMapUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingForegroundListUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingLaunchListUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingNotifyListUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingUsedListUseCase
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
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UsedAppListViewModel @Inject constructor(
    private val getPagingUsedListUseCase: GetPagingUsedListUseCase,
    private val getPagingForegroundListUseCase: GetPagingForegroundListUseCase,
    private val getPagingNotifyListUseCase: GetPagingNotifyListUseCase,
    private val getPagingLaunchListUseCase: GetPagingLaunchListUseCase,
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
            UsedAppEvent.RefreshAppUsageInfo -> { _state.update { UsedAppListState()
                }
                viewModelScope.launch {
                    getApplicationsInfo()
                }
            }

            is UsedAppEvent.GetUsageEvent -> {
                getEventList(option)
            }

            is UsedAppEvent.ChangeLoadingInfo -> {
                _state.update {
                    it.copy(isLoading = false)
                }
            }

            is UsedAppEvent.ChangeDate -> {
                _state.update {
                    it.copy(
                        displayDate = if (option.date == LocalDate.now()) "오늘"
                        else option.date.toString()
                    )
                }
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
                            appInfoList = getPagingForegroundListUseCase().cachedIn(viewModelScope),
                            sortOption = option
                        )
                    }
                }

                is UsedAppEvent.GetUsageEvent.AppLaunchEvent -> {
                    _state.update {
                        it.copy(
                            appInfoList = getPagingLaunchListUseCase().cachedIn(viewModelScope),
                            sortOption = option
                        )
                    }
                }

                is UsedAppEvent.GetUsageEvent.AppNotifyEvent -> {
                    _state.update {
                        it.copy(
                            appInfoList = getPagingNotifyListUseCase().cachedIn(viewModelScope),
                            sortOption = option
                        )
                    }
                }

                is UsedAppEvent.GetUsageEvent.AppUsageEvent -> {
                    _state.update {
                        it.copy(
                            appInfoList = getPagingUsedListUseCase().cachedIn(viewModelScope),
                            sortOption = option
                        )
                    }
                }
            }
        }
    }
}