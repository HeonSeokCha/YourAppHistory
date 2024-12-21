package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.getUsagePermission
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.domain.usecase.GetAppIconMapUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingAppDetailUseCase
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import com.chs.yourapphistory.presentation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AppUsageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPagingAppDetailUseCase: GetPagingAppDetailUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase,
    private val getInstallAppInfoUseCase: InsertInstallAppInfoUseCase
) : ViewModel() {

    var state by mutableStateOf(AppUsageDetailState())
        private set

    private val targetPackageName: String =
        savedStateHandle.toRoute<Screen.ScreenAppUsageDetail>().targetPackageName
    private val targetDate: LocalDate =
        savedStateHandle.toRoute<Screen.ScreenAppUsageDetail>().targetDate.toLocalDate()

    init {
        getPackageUsageInfo()
    }

    fun refreshUsageInfo() {
        viewModelScope.launch {
            state = state.copy(pagingDetailInfo = null)
            getApplicationsInfo()
            getPackageUsageInfo()
        }
    }

    private fun getPackageUsageInfo() {
        state = state.copy(
            pagingDetailInfo = getPagingAppDetailUseCase(
                packageName = targetPackageName,
                targetDate = targetDate
            ).cachedIn(viewModelScope),
            targetDate = targetDate
        )
    }

    private suspend fun getApplicationsInfo() {
        withContext(Dispatchers.IO) {
            awaitAll(
                async { getInstallAppInfoUseCase() },
                async { insertAppUsageInfoUseCase() },
            )
        }
    }
}