package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.common.getUntilDateList
import com.chs.yourapphistory.domain.usecase.GetDayAppUsageListUseCase
import com.chs.yourapphistory.domain.usecase.GetDayUseAppListUseCase
import com.chs.yourapphistory.domain.usecase.GetLastCollectDayUseCase
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UsedAppListViewModel @Inject constructor(
    private val getLastCollectDayUseCase: GetLastCollectDayUseCase,
    private val getDayUseAppListUseCase: GetDayUseAppListUseCase,
    private val insertInstallAppInfoUseCase: InsertInstallAppInfoUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UsedAppListState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    localDateList = getUntilDateList(getLastCollectDayUseCase())
                )
            }
        }
    }

    suspend fun insertInfo() {
        withContext(Dispatchers.IO) {
            val appInfo = async { insertInstallAppInfoUseCase() }
            val appUsage = async { insertAppUsageInfoUseCase() }
            awaitAll(appInfo, appUsage)
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
                                appInfoList = resource.data
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

    fun changeDate(idx: Int) {
        _state.update {
            it.copy(
                targetDate = it.localDateList[idx]
            )
        }
    }
}