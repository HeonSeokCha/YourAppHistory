package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.domain.model.UsageEventType
import com.chs.yourapphistory.domain.usecase.GetDayForegroundListUseCase
import com.chs.yourapphistory.domain.usecase.GetDayLaunchListUseCase
import com.chs.yourapphistory.domain.usecase.GetDayNotifyListUseCase
import com.chs.yourapphistory.domain.usecase.GetDayUsedListUseCase
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppEffect.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import java.time.LocalDate

@KoinViewModel
class UsedAppListViewModel(
    @InjectedParam private val usageEventType: UsageEventType,
    @InjectedParam private val targetDateMilli: Long,
    private val getDayUsedListUseCase: GetDayUsedListUseCase,
    private val getDayForegroundListUseCase: GetDayForegroundListUseCase,
    private val getDayNotifyListUseCase: GetDayNotifyListUseCase,
    private val getDayLaunchListUseCase: GetDayLaunchListUseCase,
) : ViewModel() {

    private val _effect: Channel<UsedAppEffect> = Channel(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    private val _state = MutableStateFlow(UsedAppListState())
    val state = _state
        .onStart {
            changeDate(targetDateMilli.toLocalDate())
            getEventList(usageEventType)
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
            is UsedAppIntent.ChangeSearchQuery -> filterList(intent.query)
        }
    }

    private fun filterList(query: String) {
        _state.update {
            if (query.isEmpty()) {
                return@update it.copy(list = it.originList)
            }

            it.copy(list = it.originList.filter {
                it.first.label.contains(
                    query,
                    ignoreCase = true
                )
            })
        }
    }

    private fun getEventList(usageEventType: UsageEventType) {
        _state.update { it.copy(isLoading = true, isShowFilterDialog = false) }
        viewModelScope.launch {
            _state.update {
                val list = when (usageEventType) {
                    UsageEventType.UsageEvent -> getDayUsedListUseCase(targetDateMilli)
                    UsageEventType.ForegroundUsageEvent -> getDayForegroundListUseCase(targetDateMilli)
                    UsageEventType.NotifyEvent -> getDayNotifyListUseCase(targetDateMilli)
                    UsageEventType.LaunchEvent -> getDayLaunchListUseCase(targetDateMilli)
                }
                it.copy(
                    sortOption = usageEventType,
                    originList = list,
                    list = list,
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