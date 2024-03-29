package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.data.model.UsageEventType
import com.chs.yourapphistory.domain.usecase.GetAppIconMapUseCase
import com.chs.yourapphistory.domain.usecase.GetDayForegroundUsedAppListUseCase
import com.chs.yourapphistory.domain.usecase.GetDayNotifyAppListUseCase
import com.chs.yourapphistory.domain.usecase.GetDayUsedAppListUseCase
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UsedAppListViewModel @Inject constructor(
    private val getDayUsedAppListUseCase: GetDayUsedAppListUseCase,
    private val getDayForegroundUsedAppListUseCase: GetDayForegroundUsedAppListUseCase,
    private val getDayNotifyAppListUseCase: GetDayNotifyAppListUseCase,
    private val insertInstallAppInfoUseCase: InsertInstallAppInfoUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase,
    private val getAppIconMapUseCase: GetAppIconMapUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UsedAppListState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                insertInfo()
            }

            _state.update {
                it.copy(
                    appInfoList = getDayUsedAppListUseCase(),
                    appIconList = getAppIconMapUseCase()
                )
            }
        }
    }


    fun changeSortOption(option: UsageEventType) {
        when (option) {
            UsageEventType.AppUsageEvent -> {
                _state.update {
                    it.copy(
                        appInfoList = getDayUsedAppListUseCase(),
                        sortOption = option
                    )
                }
            }

            UsageEventType.AppForegroundUsageEvent -> {
                _state.update {
                    it.copy(
                        appInfoList = getDayForegroundUsedAppListUseCase(),
                        sortOption = option
                    )
                }
            }

            UsageEventType.AppNotifyEvent -> {
                _state.update {
                    it.copy(
                        appInfoList = getDayNotifyAppListUseCase(),
                        sortOption = option
                    )
                }
            }
        }
    }

    private suspend fun insertInfo() {
        withContext(Dispatchers.IO) {
            awaitAll(
                async { insertInstallAppInfoUseCase() },
                async { insertAppUsageInfoUseCase() }
            )
        }
    }
}