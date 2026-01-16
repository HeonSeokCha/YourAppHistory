package com.chs.yourapphistory.presentation.screen.total_summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.reverseDateUntilWeek
import com.chs.yourapphistory.domain.usecase.GetMinimumTimeUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingWeeklyTotalInfoUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.time.LocalDate
import kotlin.collections.chunked

@KoinViewModel
class TotalSummaryViewModel(
    getPagingWeeklyTotalInfoUseCase: GetPagingWeeklyTotalInfoUseCase,
    private val getMinimumTimeUseCase: GetMinimumTimeUseCase
): ViewModel() {

    private val dateNow = LocalDate.now()
    private val _state = MutableStateFlow(TotalSummaryState())
    val state = _state
        .onStart { getDateRangeList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    private val _effect: Channel<TotalSummaryEffect> = Channel(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val pagingList = getPagingWeeklyTotalInfoUseCase().cachedIn(viewModelScope)

    private fun getDateRangeList() {
        viewModelScope.launch {
            _state.update {
                val minDate = getMinimumTimeUseCase()
                val dateList = minDate
                    .reverseDateUntilWeek(dateNow)

                val splitList = dateList.chunked(7)

                it.copy(
                    minDate = minDate,
                    dateList = splitList
                )
            }
        }
    }

    fun handleIntent(intent: TotalSummaryIntent) {
        when (intent) {
            is TotalSummaryIntent.ClickPackageName -> {
                _effect.trySend(TotalSummaryEffect.NavigateUsageDetail(intent.packageName))
            }
            TotalSummaryIntent.Loading -> _state.update { it.copy(loading = true) }
            TotalSummaryIntent.LoadComplete -> _state.update { it.copy(loading = false) }
            is TotalSummaryIntent.OnChangeDateCurrentPage -> _state.update { it.copy(dateCurrentPage = intent.page) }
            is TotalSummaryIntent.OnChangeTargetDateIdx -> changeDate(intent.idx)
            TotalSummaryIntent.Error -> TODO()
        }
    }

    private fun changeDate(idx: Pair<Int, Int>) {
        _state.update {
            val date = it.dateList[idx.first][idx.second]
            when {
                date > dateNow -> {
                    it.copy(
                        displayDate = dateNow,
                        dateIdx = idx.first to it.dateList[0].indexOf(dateNow)
                    )
                }

                date < it.minDate -> {
                    it.copy(
                        displayDate = it.minDate,
                        dateIdx = idx.first to it.dateList[it.dateList.size - 1].indexOf(it.minDate)
                    )
                }

                else -> {
                    it.copy(
                        displayDate = date,
                        dateIdx = idx
                    )
                }
            }
        }
    }
}