package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.SortType
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _effect: Channel<UsedAppEffect> = Channel(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    private val sortOptionState = MutableStateFlow(SortType.UsageEvent)

    private val _state = MutableStateFlow(UsedAppListState())
    val state = _state
        .onStart { getApplicationsInfo() }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            _state.value
        )

    val pagingList = sortOptionState
        .flatMapLatest {
            getEventList(it)
        }.cachedIn(viewModelScope)

    private suspend fun getApplicationsInfo() {
        withContext(Dispatchers.IO) {
            awaitAll(
                async { getInstallAppInfoUseCase() },
                async { insertAppUsageInfoUseCase() },
                async {
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            appIconList = getAppIconMapUseCase(),
                            sortOption = _state.value.sortOption
                        )
                    }
                }
            )
        }
    }

    fun handleIntent(intent: UsedAppIntent) {
        when (intent) {
            is UsedAppIntent.ChangeDate -> {
                _state.update {
                    it.copy(displayDate = if (intent.date == LocalDate.now()) "오늘" else intent.date.toString())
                }
            }

            is UsedAppIntent.ClickAppInfo -> {
                _effect.trySend(
                    UsedAppEffect.NavigateAppDetail(
                        appInfo = intent.appInfo,
                        targetDate = intent.targetDate.toMillis()
                    )
                )
            }

            UsedAppIntent.Error -> _effect.trySend(UsedAppEffect.ShowPagingError)
            is UsedAppIntent.OnChangeSort -> {
                _state.update { it.copy(sortOption = intent.sort, isShowFilterDialog = false) }
                sortOptionState.update { intent.sort }
            }

            is UsedAppIntent.OnShowSortDialog -> _state.update { it.copy(isShowFilterDialog = intent.value) }
            UsedAppIntent.Loading -> _state.update { it.copy(isLoading = true) }
            UsedAppIntent.LoadComplete -> _state.update { it.copy(isLoading = false) }
            UsedAppIntent.Appending -> _state.update { it.copy(isAppending = true) }
            UsedAppIntent.AppendComplete -> _state.update { it.copy(isAppending = false) }
        }
    }

    private fun getEventList(sortType: SortType): Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>> {
        return when (sortType) {
            SortType.UsageEvent -> getPagingUsedListUseCase()
            SortType.ForegroundUsageEvent -> getPagingForegroundListUseCase()
            SortType.NotifyEvent -> getPagingNotifyListUseCase()
            SortType.LaunchEvent -> getPagingLaunchListUseCase()
        }
    }
}