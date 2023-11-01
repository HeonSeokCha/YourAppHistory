package com.example.yourapphistory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourapphistory.common.getUntilDateList
import com.example.yourapphistory.data.ApplicationInfoSource
import com.example.yourapphistory.domain.usecase.GetDayAppUsageInfoUseCase
import com.example.yourapphistory.domain.usecase.GetLastCollectDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getLastCollectDayUseCase: GetLastCollectDayUseCase,
    private val getDayAppUsageInfoUseCase: GetDayAppUsageInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    localDateList = getUntilDateList(getLastCollectDayUseCase()),
                    targetDate = LocalDate.now()
                )
            }
        }
    }

    fun getAppInfoList() {
        viewModelScope.launch {
            getDayAppUsageInfoUseCase(state.value.targetDate).collect { appUsageSummaryList ->
                _state.update {
                    it.copy(
                        appInfoList = appUsageSummaryList
                    )
                }
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