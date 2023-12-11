package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chs.yourapphistory.common.calculateSplitHourUsage
import com.chs.yourapphistory.domain.usecase.GetDayAppUsageListUseCase
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
    private val getDayAppUsageListUseCase: GetDayAppUsageListUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<AppUsageDetailState> =
        MutableStateFlow(AppUsageDetailState())
    val state: StateFlow<AppUsageDetailState> = _state.asStateFlow()

    fun getDayAppUsageList(
        packageName: String,
        date: LocalDate
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    dayUsageList = calculateSplitHourUsage(
                        getDayAppUsageListUseCase(
                            packageName = packageName,
                            date = date
                        )
                    )
                )
            }
        }
    }

    fun changeDate(date: LocalDate) {
        _state.update {
            it.copy(
                targetDate = date
            )
        }
    }
}