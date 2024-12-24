package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.domain.usecase.GetPagingAppDetailUseCase
import com.chs.yourapphistory.presentation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AppUsageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPagingAppDetailUseCase: GetPagingAppDetailUseCase,
//    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase,
//    private val getInstallAppInfoUseCase: InsertInstallAppInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AppUsageDetailState())
    val state = _state
        .onStart {
            getPackageUsageInfo(targetDate)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    private val targetPackageName: String =
        savedStateHandle.toRoute<Screen.ScreenAppUsageDetail>().targetPackageName
    private val targetDate: LocalDate =
        savedStateHandle.toRoute<Screen.ScreenAppUsageDetail>().targetDate.toLocalDate()

    fun changeEvent(event: AppUsageDetailEvent) {
        when (event) {
            is AppUsageDetailEvent.OnChangeTargetDate -> {
                getPackageUsageInfo(event.date)
            }

            else -> Unit
        }
    }

    private fun getPackageUsageInfo(date: LocalDate) {
        _state.update {
            it.copy(
                pagingDetailInfo = getPagingAppDetailUseCase(
                    packageName = targetPackageName,
                    targetDate = date
                ).cachedIn(viewModelScope),
                targetDate = date
            )
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