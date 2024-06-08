package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.data.model.UsageEventType
import com.chs.yourapphistory.domain.usecase.GetAppIconMapUseCase
import com.chs.yourapphistory.domain.usecase.GetDayPagingForegroundUsedUseCase
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
    private val insertInstallAppInfoUseCase: InsertInstallAppInfoUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase,
    private val getAppIconMapUseCase: GetAppIconMapUseCase
) : ViewModel() {

    var state by mutableStateOf(UsedAppListState())
        private set

    init {
        viewModelScope.launch {
            insertInfo()
            getUsedAppList()
        }
    }


    fun changeSortOption(option: UsageEventType) {
        state = when (option) {
            UsageEventType.AppUsageEvent -> {
                state.copy(
                    appInfoList = getDayPagingUsedUseCase(),
                    sortOption = option
                )
            }

            UsageEventType.AppForegroundUsageEvent -> {
                state.copy(
                    appInfoList = getDayPagingForegroundUsedUseCase(),
                    sortOption = option
                )
            }

            UsageEventType.AppNotifyEvent -> {
                state.copy(
                    appInfoList = getDayPagingNotifyUseCase(),
                    sortOption = option
                )
            }
        }
    }


    private fun getUsedAppList() {
        viewModelScope.launch {
            state = state.copy(
                appInfoList = getDayPagingUsedUseCase().cachedIn(viewModelScope),
            )
        }
    }

    private suspend fun insertInfo() {
        withContext(Dispatchers.IO) {
            awaitAll(
                async { insertInstallAppInfoUseCase() },
                async { insertAppUsageInfoUseCase() },
                async {
                    state = state.copy(
                        appIconList = getAppIconMapUseCase()
                    )
                }
            )
        }
    }
}