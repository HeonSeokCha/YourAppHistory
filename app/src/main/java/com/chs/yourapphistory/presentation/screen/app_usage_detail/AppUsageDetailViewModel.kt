package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chs.yourapphistory.common.calculateSplitHourUsage
import com.chs.yourapphistory.common.getUntilDateList
import com.chs.yourapphistory.domain.usecase.GetDayAppUsageListUseCase
import com.chs.yourapphistory.domain.usecase.GetLastCollectDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AppUsageDetailViewModel @Inject constructor(
    private val getLastCollectDayUseCase: GetLastCollectDayUseCase,
    private val getDayAppUsageListUseCase: GetDayAppUsageListUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<AppUsageDetailState> =
        MutableStateFlow(AppUsageDetailState())
    val state: StateFlow<AppUsageDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    localDateList = getUntilDateList(getLastCollectDayUseCase())
                )
            }
        }
    }

    fun getDayAppUsageList(
        packageName: String,
        date: LocalDate
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    dayUsageList = calculateSplitHourUsage(
                        date = date,
                        list = getDayAppUsageListUseCase(
                            packageName = packageName,
                            date = date
                        )
                    )
                )
            }
        }
    }

    fun changeDate(idx: Int) {
        _state.update {
            it.copy(
                targetDate = it.localDateList[idx]
            )
        }
    }
}