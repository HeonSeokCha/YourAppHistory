package com.chs.yourapphistory.presentation.screen.total_summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chs.yourapphistory.common.reverseDateUntilWeek
import com.chs.yourapphistory.domain.usecase.GetMinimumTimeUseCase
import com.chs.yourapphistory.domain.usecase.GetPagingWeeklyTotalInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertAppUsageInfoUseCase
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import com.chs.yourapphistory.presentation.screen.total_summary.TotalSummaryEffect.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.time.LocalDate
import kotlin.collections.chunked

@KoinViewModel
class TotalSummaryViewModel(
    getPagingWeeklyTotalInfoUseCase: GetPagingWeeklyTotalInfoUseCase,
    private val getMinimumTimeUseCase: GetMinimumTimeUseCase,
    private val insertAppUsageInfoUseCase: InsertAppUsageInfoUseCase,
    private val getInstallAppInfoUseCase: InsertInstallAppInfoUseCase
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

    val pagingList = getPagingWeeklyTotalInfoUseCase()
        .cachedIn(viewModelScope)
        .onStart { insertUsageInfo() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            PagingData.empty()
        )

    private fun getDateRangeList() {
        viewModelScope.launch {
            _state.update {
                val minDate = getMinimumTimeUseCase()
                val dateList = minDate
                    .reverseDateUntilWeek(dateNow)

                val splitList = dateList.chunked(7)

                it.copy(
                    minDate = minDate,
                    dateList = splitList,
                    dateIdx = 0 to splitList.first().indexOf(dateNow)
                )
            }
        }
    }

    fun handleIntent(intent: TotalSummaryIntent) {
        when (intent) {
            is TotalSummaryIntent.ClickPackageName -> {
                _effect.trySend(
                    NavigateUsageDetail(
                        packageName = intent.packageName,
                        label = intent.label,
                        targetDate = intent.targetDate
                    )
                )
            }
            TotalSummaryIntent.Loading -> _state.update { it.copy(loading = true) }
            TotalSummaryIntent.LoadComplete -> _state.update { it.copy(loading = false) }
            is TotalSummaryIntent.OnChangeDateCurrentPage -> {
                _state.update { it.copy(dateCurrentPage = intent.page) }
                changeDate(intent.page to _state.value.dateIdx.second)
            }
            is TotalSummaryIntent.OnChangeTargetDateIdx -> changeDate(intent.idx)
            TotalSummaryIntent.Error -> TODO()
            is TotalSummaryIntent.ClickUsedAppList -> TODO()
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

    private suspend fun insertUsageInfo() {
        _state.update { it.copy(loading = true) }
        withContext(Dispatchers.IO) {
            launch(Dispatchers.IO) {
                getInstallAppInfoUseCase()
            }

            launch(Dispatchers.IO) {
                insertAppUsageInfoUseCase()
            }
        }
    }
}