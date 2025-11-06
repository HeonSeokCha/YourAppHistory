package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.reverseDateUntilWeek
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.domain.usecase.GetMinimumTimeUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingDailyUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingWeeklyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.time.LocalDate

@KoinViewModel
class AppUsageDetailViewModel(
    targetPackageName: String,
    targetDateMilli: Long,
    getPagingDailyUseCase: GetPagingDailyUseCase,
    getPagingWeeklyUseCase: GetPagingWeeklyUseCase,
    private val getMinimumTimeUseCase: GetMinimumTimeUseCase
) : ViewModel() {

    private val targetDate = targetDateMilli.toLocalDate()

    private val _state = MutableStateFlow(AppUsageDetailState())
    val state = _state
        .onStart { getDateRangeList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    val getDailyPagingData = getPagingDailyUseCase(
        targetDate = targetDate,
        packageName = targetPackageName
    ).cachedIn(viewModelScope)

    val getWeeklyPagingData = getPagingWeeklyUseCase(
        targetDate = targetDate,
        packageName = targetPackageName
    ).cachedIn(viewModelScope)

    fun handleIntent(intent: AppUsageDetailIntent) {
        when (intent) {
            is AppUsageDetailIntent.OnChangeTargetDate -> {
                changeDate(intent.date)
            }

            is AppUsageDetailIntent.OnChangeTargetWeek -> {
                changeWeek(intent.date)
            }

            is AppUsageDetailIntent.OnChangeViewType -> {
                _state.update { it.copy(isDailyMode = !_state.value.isDailyMode) }
            }
        }
    }

    private fun getDateRangeList() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    minDate = getMinimumTimeUseCase(),
                    dateList = getMinimumTimeUseCase()
                        .reverseDateUntilWeek(LocalDate.now())
                        .chunked(7),
                    weekList = getMinimumTimeUseCase()
                        .reverseDateUntilWeek(LocalDate.now())
                        .chunked(7)
                        .map { it.max() }
                        .chunked(5),
                    displayDate = targetDate,
                    displayWeek = targetDate.reverseDateUntilWeek(targetDate)
                )
            }
        }
    }

    private fun changeDate(date: LocalDate) {
        _state.update {
            it.copy(displayDate = date)
        }
    }

    private fun changeWeek(date: LocalDate) {
        _state.update {
            it.copy(
                displayWeek = date.reverseDateUntilWeek(date)
            )
        }
    }
}