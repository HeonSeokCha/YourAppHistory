package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.Resource
import com.chs.yourapphistory.domain.usecase.GetDayPagingUseAppListUseCase
import com.chs.yourapphistory.domain.usecase.GetLastCollectDayUseCase
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UsedAppListViewModel @Inject constructor(
    private val getDayPagingUseAppListUseCase: GetDayPagingUseAppListUseCase,
    private val insertInstallAppInfoUseCase: InsertInstallAppInfoUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UsedAppListState())
    val state = _state.asStateFlow()

    suspend fun insertInfo() {
        withContext(Dispatchers.IO) {
            val appInfo = async { insertInstallAppInfoUseCase() }
            val appUsage = async { insertAppUsageInfoUseCase() }
            awaitAll(appInfo, appUsage)
        }
    }

    fun getDayUseAppInfoList() {
        _state.update {
            it.copy(
                appInfoList = getDayPagingUseAppListUseCase().cachedIn(viewModelScope)
            )
        }
    }
}