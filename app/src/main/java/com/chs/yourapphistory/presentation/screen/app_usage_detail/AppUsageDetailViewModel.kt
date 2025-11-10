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
            }
        }
    }

    private fun getDateRangeList() {
        viewModelScope.launch {
            _state.update {
                val minDate = getMinimumTimeUseCase()
                val dateList = getMinimumTimeUseCase()
                    .reverseDateUntilWeek(LocalDate.now())
                    .chunked(7)
                val weekList = getMinimumTimeUseCase()
                    .reverseDateUntilWeek(LocalDate.now())
                    .chunked(7)
                    .map { it.max() }
                    .chunked(5)
                it.copy(
                    minDate = minDate,
                    dateList = dateList,
                    weekList = weekList,
                    displayDate = targetDate,
                    dateIdx = (dateList.flatten().indexOf(targetDate) / 7).run {
                        this to dateList[this].indexOf(targetDate)
                    },
                    displayWeek = targetDate.reverseDateUntilWeek(targetDate)
                )
            }
        }
    }

    private fun changeDate(idx: Pair<Int, Int>) {
        _state.update {
            val date = it.dateList[idx.first][idx.second]
            when {
                date > LocalDate.now() -> {
                    chsLog((idx.first to it.dateList[0].indexOf(LocalDate.now())).toString())
                    it.copy(
                        displayDate = LocalDate.now(),
                        dateIdx = idx.first to it.dateList[0].indexOf(LocalDate.now())
                    )
                }

                date < it.minDate -> {
                    chsLog((idx.first to it.dateList[it.dateList.size - 1].indexOf(it.minDate)).toString())
                    it.copy(
                        displayDate = it.minDate,
                        dateIdx = idx.first to it.dateList[it.dateList.size - 1].indexOf(it.minDate)
                    )
                }

                else -> {
                    chsLog(idx.toString())
                    it.copy(displayDate = date)
                }
            }
        }
    }

    private fun changeWeek(idx: Pair<Int, Int>) {
        _state.update {
            val date = it.weekList[idx.first][idx.second]
            it.copy(
                displayWeek = date.reverseDateUntilWeek(date)
            )
        }
    }
}