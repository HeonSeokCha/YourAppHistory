package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.getUntilDateList
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.domain.usecase.GetAppLaunchCountUseCase
import com.chs.yourapphistory.domain.usecase.GetAppUsageTimeZoneInfoUseCase
import com.chs.yourapphistory.domain.usecase.GetLastCollectDayUseCase
import com.chs.yourapphistory.domain.usecase.GetPackageLabelUseCase
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
    savedStateHandle: SavedStateHandle,
    private val getLastCollectDayUseCase: GetLastCollectDayUseCase,
    private val getAppUsageTimeZoneInfoUseCase: GetAppUsageTimeZoneInfoUseCase,
    private val getAppLaunchCountUseCase: GetAppLaunchCountUseCase,
    private val getPackageLabelUseCase: GetPackageLabelUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<AppUsageDetailState> =
        MutableStateFlow(AppUsageDetailState())
    val state: StateFlow<AppUsageDetailState> = _state.asStateFlow()

    private val targetPackageName: String = savedStateHandle["packageName"] ?: ""
    private val targetDate: LocalDate = (savedStateHandle["targetDate"] ?: 0L).toLocalDate()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    targetDate = targetDate,
                    datesList = getUntilDateList(
                        getLastCollectDayUseCase()
                    ),
                    targetPackageLabel = getPackageLabelUseCase(targetPackageName)
                )
            }
        }
    }

    fun getDayAppUsageList(date: LocalDate) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    dayUsageList = getAppUsageTimeZoneInfoUseCase(
                        date = date,
                        packageName = targetPackageName
                    ),
                    launchCount = getAppLaunchCountUseCase(
                        date = date,
                        packageName = targetPackageName
                    )
                )
            }
        }
    }

    fun changeDate(localDate: LocalDate) {
        _state.update {
            it.copy(
                targetDate = localDate
            )
        }
    }
}