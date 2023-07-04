package com.example.yourapphistory.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()


    fun initStartDate(timeStamp: Long?) {
        _state.update {
            it.copy(
                startDate = timeStamp
            )
        }
    }

    fun initEndDate(timeStamp: Long?) {
        _state.update {
            it.copy(
                endDate = timeStamp
            )
        }
    }
}