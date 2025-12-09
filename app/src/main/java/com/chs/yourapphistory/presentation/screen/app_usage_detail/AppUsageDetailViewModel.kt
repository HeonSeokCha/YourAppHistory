package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.chsLog
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
    private val dateNow = LocalDate.now()

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
            is AppUsageDetailIntent.OnChangeTargetDateIdx -> {
                changeDate(intent.idx)
            }

            is AppUsageDetailIntent.OnChangeTargetWeekIdx -> {
                changeWeek(intent.idx)
            }

            is AppUsageDetailIntent.OnChangeViewType -> {
                _state.update { it.copy(isWeeklyMode = !it.isWeeklyMode) }
            }

            AppUsageDetailIntent.DateLoading -> _state.update { it.copy(isDateLoading = true) }
            AppUsageDetailIntent.DateLoadComplete -> _state.update { it.copy(isDateLoading = false) }
            AppUsageDetailIntent.WeekLoading -> _state.update { it.copy(isWeekLoading = true) }
            AppUsageDetailIntent.WeekLoadComplete -> _state.update { it.copy(isWeekLoading = false) }
            AppUsageDetailIntent.Error -> {}
        }
    }

    private fun getDateRangeList() {
        viewModelScope.launch {
            _state.update {
                val minDate = getMinimumTimeUseCase()
                val dateList = minDate
                    .reverseDateUntilWeek(dateNow)

                val splitList = dateList.chunked(7)

                val weekList = splitList.chunked(5)

                it.copy(
                    minDate = minDate,
                    dateList = splitList,
                    displayDate = targetDate,
                    dateIdx = (dateList.indexOf(targetDate) / 7).run {
                        this to (splitList[this].indexOf(targetDate) % 7)
                    },
                    displayWeek = targetDate.reverseDateUntilWeek(targetDate),
                    weekList = weekList,
                    weekIdx = (dateList.indexOf(targetDate) / 7).run {
                        0 to this
                    }
                )
            }
        }
    }

    private fun changeDate(idx: Pair<Int, Int>) {
        _state.update {
            val date = it.dateList[idx.first][idx.second]
            when {
                date > dateNow -> {
                    it.copy(
                        displayDate = dateNow,
                        dateIdx = idx.first to it.dateList[0].indexOf(dateNow)
                    )
                }

                date < it.minDate -> {
                    it.copy(
                        displayDate = it.minDate,
                        dateIdx = idx.first to it.dateList[it.dateList.size - 1].indexOf(it.minDate)
                    )
                }

                else -> {
                    it.copy(
                        displayDate = date,
                        dateIdx = idx
                    )
                }
            }
        }
    }

    private fun changeWeek(idx: Pair<Int, Int>) {
        _state.update { stateInfo ->
            when {

                stateInfo.weekList[idx.first].size <= idx.second -> {
                    stateInfo.copy(
                        weekIdx = idx,
                        displayWeek = stateInfo.weekList[idx.first][stateInfo.weekList[idx.first].size - 1]
                    )
                }

                else -> {
                    stateInfo.copy(
                        weekIdx = idx,
                        displayWeek = stateInfo.weekList[idx.first][idx.second]
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chsLog("ONCLEARED")
    }
}