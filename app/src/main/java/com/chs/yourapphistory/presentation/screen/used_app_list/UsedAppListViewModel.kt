package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.domain.usecase.GetDayForegroundListUseCase
import com.chs.yourapphistory.domain.usecase.GetDayLaunchListUseCase
import com.chs.yourapphistory.domain.usecase.GetDayNotifyListUseCase
import com.chs.yourapphistory.domain.usecase.GetDayUsedListUseCase
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppEffect.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.time.LocalDate

@KoinViewModel
class UsedAppListViewModel(
    sortType: SortType,
    private val targetDateMilli: Long,
    private val getDayUsedListUseCase: GetDayUsedListUseCase,
    private val getDayForegroundListUseCase: GetDayForegroundListUseCase,
    private val getDayNotifyListUseCase: GetDayNotifyListUseCase,
    private val getDayLaunchListUseCase: GetDayLaunchListUseCase,
) : ViewModel() {

    private val _effect: Channel<UsedAppEffect> = Channel(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    private val searchQueryState = MutableStateFlow("")
    private val _state = MutableStateFlow(UsedAppListState())
    val state = _state
        .onStart {
            changeDate(targetDateMilli.toLocalDate())
            getEventList(sortType)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            _state.value
        )

    fun handleIntent(intent: UsedAppIntent) {
        when (intent) {
            is UsedAppIntent.ChangeDate -> changeDate(intent.date)

            is UsedAppIntent.ClickAppInfo -> {
                _effect.trySend(
                    NavigateAppDetail(
                        appInfo = intent.appInfo,
                        targetDate = targetDateMilli
                    )
                )
            }

            UsedAppIntent.Error -> Unit
            is UsedAppIntent.OnChangeSort -> getEventList(intent.sort)
            is UsedAppIntent.OnShowSortDialog -> _state.update { it.copy(isShowFilterDialog = intent.value) }
            is UsedAppIntent.ChangeSearchQuery -> {
                _state.update {
                    if (intent.query.isEmpty()) {
                        return@update it.copy(list = it.originList)
                    }

                    it.copy(list = it.originList.filter {
                        it.first.label.contains(
                            intent.query,
                            ignoreCase = true
                        )
                    })
                }
            }
        }
    }

    private fun getEventList(sortType: SortType) {
        _state.update { it.copy(isLoading = true, isShowFilterDialog = false) }
        viewModelScope.launch {
            _state.update {
                val a = when (sortType) {
                    SortType.UsageEvent -> getDayUsedListUseCase(targetDateMilli)
                    SortType.ForegroundUsageEvent -> getDayForegroundListUseCase(targetDateMilli)
                    SortType.NotifyEvent -> getDayNotifyListUseCase(targetDateMilli)
                    SortType.LaunchEvent -> getDayLaunchListUseCase(targetDateMilli)
                }
                it.copy(
                    sortOption = sortType,
                    originList = a,
                    list = a,
                    isLoading = false
                )
            }
        }
    }

    private fun changeDate(date: LocalDate) {
        _state.update {
            it.copy(displayDate = if (date == LocalDate.now()) "오늘" else date.toString())
        }
    }
}