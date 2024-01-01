package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.domain.usecase.GetAppUsageTimeZoneInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AppUsageDetailViewModel @Inject constructor(
    private val getAppUsageTimeZoneInfoUseCase: GetAppUsageTimeZoneInfoUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<AppUsageDetailState> =
        MutableStateFlow(AppUsageDetailState())
    val state: StateFlow<AppUsageDetailState> = _state.asStateFlow()

    fun getDayAppUsageList(
        packageName: String,
        date: LocalDate
    ) {
        _state.update {
            it.copy(
                dayUsageList = getAppUsageTimeZoneInfoUseCase(
                    date = date,
                    packageName = packageName
                ).cachedIn(viewModelScope)
            )
        }
    }
}