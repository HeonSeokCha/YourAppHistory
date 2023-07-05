package com.example.yourapphistory.presentation

import androidx.lifecycle.ViewModel
import com.example.yourapphistory.data.ApplicationInfoSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val source: ApplicationInfoSource
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        _state.update {
            it.copy(
                localDateList = source.getLocalDateList(),
                targetDate = LocalDate.now()
            )
        }
    }

    fun getAppInfoList(localDate: LocalDate) {
        _state.update {
            it.copy(
                appInfoList = source.getLauncherAppInfoList(localDate)
            )
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