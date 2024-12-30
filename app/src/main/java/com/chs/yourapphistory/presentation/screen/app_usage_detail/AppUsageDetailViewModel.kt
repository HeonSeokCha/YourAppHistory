package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.ui.util.packInts
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.data.paging.GetPagingAppForegroundInfo
import com.chs.yourapphistory.domain.usecase.GetPagingAppLaunchUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingAppNotifyUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingAppUsedInfoUseCase
import com.chs.yourapphistory.presentation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AppUsageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPagingAppUsedInfoUseCase: GetPagingAppUsedInfoUseCase,
    private val getPagingAppForegroundInfo: GetPagingAppForegroundInfo,
    private val getPagingAppNotifyUseCase: GetPagingAppNotifyUseCase,
    private val getPagingAppLaunchUseCase: GetPagingAppLaunchUseCase
//    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase,
//    private val getInstallAppInfoUseCase: InsertInstallAppInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AppUsageDetailState())
    val state = _state
        .onStart {
            getPackageUsageInfo(targetDate)
            changeDate(targetDate)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            _state.value
        )

    private val targetPackageName: String =
        savedStateHandle.toRoute<Screen.ScreenAppUsageDetail>().targetPackageName
    private val targetDate: LocalDate =
        savedStateHandle.toRoute<Screen.ScreenAppUsageDetail>().targetDate.toLocalDate()

    fun changeEvent(event: AppUsageDetailEvent) {
        when (event) {
            is AppUsageDetailEvent.OnChangeTargetDate -> {
                changeDate(event.date)
            }

            else -> Unit
        }
    }

    private fun changeDate(date: LocalDate) {
        _state.update {
            it.copy(displayDate = date)
        }
    }

    private fun getPackageUsageInfo(date: LocalDate) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    pagingUsedInfo = getPagingAppUsedInfoUseCase(
                        targetDate = date,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingForegroundUsedInfo = getPagingAppUsedInfoUseCase(
                        targetDate = date,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingNotifyInfo = getPagingAppNotifyUseCase(
                        targetDate = date,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                    pagingLaunchInfo = getPagingAppLaunchUseCase(
                        targetDate = date,
                        packageName = targetPackageName
                    ).cachedIn(viewModelScope),
                )
            }
        }
    }

//    private suspend fun getApplicationsInfo() {
//        withContext(Dispatchers.IO) {
//            awaitAll(
//                async { getInstallAppInfoUseCase() },
//                async { insertAppUsageInfoUseCase() },
//            )
//        }
//    }
}