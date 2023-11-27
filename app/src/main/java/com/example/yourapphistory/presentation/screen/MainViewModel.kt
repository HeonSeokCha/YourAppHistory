package com.example.yourapphistory.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourapphistory.common.Resource
import com.example.yourapphistory.common.getUntilDateList
import com.example.yourapphistory.domain.usecase.GetDayAppUsageInfoUseCase
import com.example.yourapphistory.domain.usecase.GetDayUseAppListUseCase
import com.example.yourapphistory.domain.usecase.GetLastCollectDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getLastCollectDayUseCase: GetLastCollectDayUseCase,
    private val getDayUseAppListUseCase: GetDayUseAppListUseCase,
    private val getDayAppUsageInfoUseCase: GetDayAppUsageInfoUseCase,
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

    fun getDayUseAppInfoList(date: LocalDate) {
        viewModelScope.launch {
            getDayUseAppListUseCase(date).collectLatest { resource ->
                _state.update {
                    when (resource) {
                        is Resource.Loading -> {
                            it.copy(
                                isLoading = true
                            )
                        }

                        is Resource.Success -> {
                            it.copy(
                                isLoading = false,
                                appInfoList = resource.data ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
    }

    fun getDayAppUsageInfoList(
        date: LocalDate,
        packageName: String
    ) {
        viewModelScope.launch {
            getDayAppUsageInfoUseCase(
                date = date,
                packageName = packageName
            ).collectLatest { resource ->
                _state.update {
                    when (resource) {
                        is Resource.Loading -> {
                            it.copy(
                                isLoading = true
                            )
                        }

                        is Resource.Success -> {
                            it.copy(
                                isLoading = false,
                                appUsageList = resource.data ?: emptyList()
                            )
                        }
                    }
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