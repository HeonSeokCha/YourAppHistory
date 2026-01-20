package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.domain.usecase.GetPagingForegroundListUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingLaunchListUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingNotifyListUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingUsedListUseCase
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.time.LocalDate

@KoinViewModel
class UsedAppListViewModel(
    private val getPagingUsedListUseCase: GetPagingUsedListUseCase,
    private val getPagingForegroundListUseCase: GetPagingForegroundListUseCase,
    private val getPagingNotifyListUseCase: GetPagingNotifyListUseCase,
    private val getPagingLaunchListUseCase: GetPagingLaunchListUseCase,
) : ViewModel() {

    private val _effect: Channel<UsedAppEffect> = Channel(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    private val sortOptionState = MutableStateFlow(SortType.UsageEvent)

    private val _state = MutableStateFlow(UsedAppListState())
    val state = _state.asStateFlow()

    val pagingList = sortOptionState
        .flatMapLatest {
            getEventList(it)
        }.cachedIn(viewModelScope)

    fun handleIntent(intent: UsedAppIntent) {
        when (intent) {
            is UsedAppIntent.ChangeDate -> changeDate(intent.date)

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
        changeDate(LocalDate.now())
        return when (sortType) {
            SortType.UsageEvent -> getPagingUsedListUseCase()
            SortType.ForegroundUsageEvent -> getPagingForegroundListUseCase()
            SortType.NotifyEvent -> getPagingNotifyListUseCase()
            SortType.LaunchEvent -> getPagingLaunchListUseCase()
        }
    }

    private fun changeDate(date: LocalDate) {
        _state.update {
            it.copy(displayDate = if (date == LocalDate.now()) "오늘" else date.toString())
        }
    }
}