package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.reverseDateUntilWeek
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.domain.usecase.GetMinimumTimeUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingDailyForegroundUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingDailyLaunchUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingDailyNotifyUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingDailyUsedUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingWeeklyForegroundUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingWeeklyLaunchUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingWeeklyNotifyUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingWeeklyUsedUseCase
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
    private val targetPackageName: String,
    targetDateMilli: Long,
    private val getPagingDailyUsedUseCase: GetPagingDailyUsedUseCase,
    private val getPagingDailyForegroundUseCase: GetPagingDailyForegroundUseCase,
    private val getPagingDailyNotifyUseCase: GetPagingDailyNotifyUseCase,
    private val getPagingDailyLaunchUseCase: GetPagingDailyLaunchUseCase,
    private val getPagingWeeklyUsedUseCase: GetPagingWeeklyUsedUseCase,
    private val getPagingWeeklyForegroundUseCase: GetPagingWeeklyForegroundUseCase,
    private val getPagingWeeklyNotifyUseCase: GetPagingWeeklyNotifyUseCase,
    private val getPagingWeeklyLaunchUseCase: GetPagingWeeklyLaunchUseCase,
    private val getMinimumTimeUseCase: GetMinimumTimeUseCase
) : ViewModel() {

    private val targetDate = targetDateMilli.toLocalDate()

    private val _state = MutableStateFlow(AppUsageDetailState())
    val state = _state.onStart {
            getDateRangeList()
            getPackageUsageInfo(targetDate)
            changeDate(targetDate)
            changeWeek(targetDate)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            _state.value
        )

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

            AppUsageDetailIntent.OnBackClick -> {

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
                        .chunked(5)
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

    private fun getPackageUsageInfo(date: LocalDate) {
            _state.update {
                it.copy(
                    pagingDailyUsedInfo = getPagingDailyUsedUseCase(
                        targetDate = date,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingDailyForegroundUsedInfo = getPagingDailyForegroundUseCase(
                        targetDate = date,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingDailyNotifyInfo = getPagingDailyNotifyUseCase(
                        targetDate = date,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingDailyLaunchInfo = getPagingDailyLaunchUseCase(
                        targetDate = date,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingWeeklyUsedInfo = getPagingWeeklyUsedUseCase(
                        targetDate = targetDate,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingWeeklyForegroundInfo = getPagingWeeklyForegroundUseCase(
                        targetDate = targetDate,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingWeeklyNotifyInfo = getPagingWeeklyNotifyUseCase(
                        targetDate = targetDate,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingWeeklyLaunchInfo = getPagingWeeklyLaunchUseCase(
                        targetDate = targetDate,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope)
                )
            }
    }
}