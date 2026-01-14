package com.chs.yourapphistory.presentation.screen.total_summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chs.yourapphistory.domain.model.SortType

@Composable
fun TotalSummaryScreenRoot(
    viewModel: TotalSummaryViewModel,
    onNavigateUsageDetail: (String) -> Unit,
    onNavigateUsedAppList: (SortType) -> Unit
) {
    val state: TotalSummaryState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TotalSummaryEffect.NavigateUsageDetail -> onNavigateUsageDetail(effect.packageName)
                is TotalSummaryEffect.NavigateUsedAppList -> onNavigateUsedAppList(effect.sortType)
            }
        }
    }

    TotalSummaryScreen(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

@Composable
fun TotalSummaryScreen(
    state: TotalSummaryState,
    onIntent: (TotalSummaryIntent) -> Unit
) {

}