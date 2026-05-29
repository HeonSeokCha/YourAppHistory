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
    targetPackageNameInfo: Pair<String, String>,
    targetDateMilli: Long,
    getPagingDailyUseCase: GetPagingDailyUseCase,
    getPagingWeeklyUseCase: GetPagingWeeklyUseCase,
    private val getMinimumTimeUseCase: GetMinimumTimeUseCase
) : ViewModel() {

    private val targetDate = targetDateMilli.toLocalDate()
    private val dateNow = LocalDate.now()
    private val targetPackageName = targetPackageNameInfo.first
    private val targetPackageLabel = targetPackageNameInfo.second

    private val _state = MutableStateFlow(AppUsageDetailState())
    val state = _state
        .onStart {
            getDateRangeList()
            _state.update { it.copy(packageLabel = targetPackageLabel) }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
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

            AppUsageDetailIntent.DateLoading -> {
                _state.update { it.copy(isDateLoading = true) }
            }

            is AppUsageDetailIntent.DateLoadComplete -> {
                _state.update { it.copy(isDateLoading = false, datePagerInitIdx = intent.initIdx) }
            }

            AppUsageDetailIntent.WeekLoading -> {
                _state.update { it.copy(isWeekLoading = true) }
            }

            AppUsageDetailIntent.WeekLoadComplete -> {
                _state.update { it.copy(isWeekLoading = false) }
            }

            is AppUsageDetailIntent.OnChangeWeekCurrentPage -> {
                _state.update { it.copy(weekCurrentPage = intent.page) }
            }

            AppUsageDetailIntent.Error -> Unit
            is AppUsageDetailIntent.OnChangeDate -> {
                val idx = _state.value.dateList.flatten().indexOf(intent.date)
                _state.update { it.copy(dateIdx = idx / 7 to idx % 7, displayDate = intent.date) }
            }
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
                        chsLog(this to (splitList[this].indexOf(targetDate) % 7))
                        this to (splitList[this].indexOf(targetDate) % 7)
                    },
                    displayWeek = targetDate.reverseDateUntilWeek(targetDate),
                    weekList = weekList,
                    weekIdx = (dateList.indexOf(targetDate) / 7).run { (this / 5) to this }
                )
            }
        }
    }

    private fun changeDate(idx: Pair<Int, Int>) {
        chsLog("changeDate $idx")
        _state.update {
            val idx2 = if (idx.second == -1 ||idx.second == 7) {
                if (idx.second == -1) {
                    idx.first - 1 to 6
                } else {
                    idx.first + 1 to 0
                }
            } else {
                idx
            }
            val date = it.dateList[idx2.first][idx2.second]
            when {
                date > dateNow -> {
                    it.copy(
                        displayDate = dateNow,
                        dateIdx = idx2.first to it.dateList[0].indexOf(dateNow)
                    )
                }

                date < it.minDate -> {
                    it.copy(
                        displayDate = it.minDate,
                        dateIdx = idx2.first to it.dateList[it.dateList.size - 1].indexOf(it.minDate)
                    )
                }

                else -> {
                    it.copy(
                        displayDate = date,
                        dateIdx = idx2
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
                        weekIdx = idx.first to stateInfo.weekList[idx.first].size - 1,
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