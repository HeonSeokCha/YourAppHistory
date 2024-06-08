package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AppUsageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var state by mutableStateOf(AppUsageDetailState())
        private set

    private val targetPackageName: String =
        savedStateHandle[Constants.KEY_TARGET_PACKAGE_NAME] ?: ""
    private val targetDate: LocalDate =
        (savedStateHandle[Constants.KEY_TARGET_DATE] ?: 0L).toLocalDate()

    init {
        viewModelScope.launch {
            state = state.copy(

            )
        }
    }
}