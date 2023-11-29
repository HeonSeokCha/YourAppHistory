package com.chs.yourapphistory.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.common.getUntilDateList
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import com.chs.yourapphistory.domain.usecase.GetDayAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.GetDayUseAppListUseCase
import com.chs.yourapphistory.domain.usecase.GetLastCollectDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
            getDayUseAppListUseCase(date).collect { resource ->
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

                        is Resource.Error -> {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.exception
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

                        is Resource.Error -> {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.exception
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