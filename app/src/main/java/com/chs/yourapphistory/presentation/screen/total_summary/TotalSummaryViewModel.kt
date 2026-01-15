package com.chs.yourapphistory.presentation.screen.total_summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chs.yourapphistory.domain.usecase.GetPagingWeeklyTotalInfoUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TotalSummaryViewModel(
    private val getPagingWeeklyTotalInfoUseCase: GetPagingWeeklyTotalInfoUseCase
): ViewModel() {

    private val _state = MutableStateFlow(TotalSummaryState())
    val state = _state.asStateFlow()

    private val _effect: Channel<TotalSummaryEffect> = Channel(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val pagingList = getPagingWeeklyTotalInfoUseCase().cachedIn(viewModelScope)

    fun handleIntent(intent: TotalSummaryIntent) {
        when (intent) {
            is TotalSummaryIntent.ClickPackageName -> {
                _effect.trySend(TotalSummaryEffect.NavigateUsageDetail(intent.packageName))
            }
            TotalSummaryIntent.Loading -> _state.update { it.copy(loading = true) }
            TotalSummaryIntent.LoadComplete -> _state.update { it.copy(loading = false) }
            is TotalSummaryIntent.OnChangeDateCurrentPage -> _state.update { it.copy(dateCurrentPage = intent.page) }
            is TotalSummaryIntent.OnChangeTargetDateIdx -> _state.update { it.copy(dateIdx = intent.idx) }
            TotalSummaryIntent.Error -> TODO()
        }
    }
}