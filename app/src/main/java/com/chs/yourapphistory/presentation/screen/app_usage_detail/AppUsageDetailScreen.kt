package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.domain.model.UsageEventType
import com.chs.yourapphistory.presentation.screen.total_summary.ItemLoadingFromTotal
import java.time.LocalDate

@Composable
fun AppUsageDetailScreenRoot(
    viewModel: AppUsageDetailViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dailyPagingItems = viewModel.getDailyPagingData.collectAsLazyPagingItems()
    val weeklyPagingItems = viewModel.getWeeklyPagingData.collectAsLazyPagingItems()

    AppUsageDetailScreen(
        state = state,
        dailyPagingItems = dailyPagingItems,
        weeklyPagingItems = weeklyPagingItems,
        onIntent = viewModel::handleIntent
    )
}

@Composable
fun AppUsageDetailScreen(
    state: AppUsageDetailState,
    dailyPagingItems: LazyPagingItems<Pair<LocalDate, Map<UsageEventType, List<Pair<Int, Int>>>>>,
    weeklyPagingItems: LazyPagingItems<Map<UsageEventType, List<Pair<LocalDate, Int>>>>,
    onIntent: (AppUsageDetailIntent) -> Unit
) {
    /* date related variables */
    val datePagerState = if (state.isDateLoading) {
        rememberPagerState(pageCount = { state.dateList.count() })
    } else {
        rememberPagerState(
            pageCount = { state.dateList.count() },
            initialPage = state.dateIdx.first
        )
    }

    LaunchedEffect(dailyPagingItems.itemSnapshotList, dailyPagingItems.loadState.refresh) {
        if (dailyPagingItems.loadState.refresh !is LoadState.NotLoading) {
            onIntent(AppUsageDetailIntent.DateLoading)
            return@LaunchedEffect
        }
        if (dailyPagingItems.itemCount == 0) return@LaunchedEffect
        val initIdx = dailyPagingItems.itemSnapshotList.map { it?.first }.indexOf(state.displayDate)
        chsLog("NotLoading $initIdx")
        onIntent(AppUsageDetailIntent.DateLoadComplete(initIdx))
    }

    LaunchedEffect(datePagerState.currentPage, datePagerState.isScrollInProgress) {
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        if (datePagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect
        if (datePagerState.isScrollInProgress) return@LaunchedEffect
        onIntent(AppUsageDetailIntent.OnClickDate(datePagerState.currentPage to state.dateIdx.second))
    }

    LaunchedEffect(state.dateIdx) {
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        val page = state.dateIdx.run { (this.first * 7) + this.second }
        chsLog("dateIdx ${state.dateIdx} $page")
        datePagerState.scrollToPage(state.dateIdx.first)
//        onIntent(AppUsageDetailIntent.OnChangeDateCurrentPage(page))
    }

    /* date related variables end*/

    /* week related variables */
    val weekPagerState = rememberPagerState(pageCount = { state.weekList.count() })

    LaunchedEffect(weeklyPagingItems.loadState.refresh) {
        when (weeklyPagingItems.loadState.refresh) {
            is LoadState.Loading -> onIntent(AppUsageDetailIntent.WeekLoading)
            is LoadState.NotLoading -> onIntent(AppUsageDetailIntent.WeekLoadComplete)
            is LoadState.Error -> onIntent(AppUsageDetailIntent.Error)
        }
    }

    LaunchedEffect(weekPagerState.currentPage, weekPagerState.isScrollInProgress) {
        if (state.weekList.isEmpty()) return@LaunchedEffect
        if (weekPagerState.isScrollInProgress) return@LaunchedEffect
        onIntent(
            AppUsageDetailIntent.OnChangeTargetWeekIdx(weekPagerState.currentPage to state.weekIdx.second)
        )
    }
    LaunchedEffect(state.weekIdx) {
        val page = state.weekIdx.run { (this.first * 5) + this.second }

        weekPagerState.scrollToPage(state.weekIdx.first)
        onIntent(AppUsageDetailIntent.OnChangeWeekCurrentPage(page))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        ItemDateHeader(
            state = state,
            onIntent = onIntent
        )

        if (state.isWeeklyMode) {
            if (state.isWeekLoading) {
                ItemLoadingFromTotal()
            } else {
                ItemWeekList(
                    state = weekPagerState,
                    targetWeek = state.displayWeek,
                    item = state.weekList,
                    onClick = { onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(it)) }
                )

                ItemWeeklyPagingInfo(
                    state = state,
                    weeklyPagingItems = weeklyPagingItems,
                    onIntent = onIntent
                )
            }
        } else {
//            if (state.isDateLoading) {
//                ItemLoadingFromTotal()
//            } else {
                ItemDateList(
                    state = datePagerState,
                    minDate = state.minDate,
                    targetDate = state.displayDate,
                    item = state.dateList,
                    onClick = { onIntent(AppUsageDetailIntent.OnClickDate(it)) }
                )

                ItemDailyPagingInfo(
                    state = state,
                    dailyPagingItems = dailyPagingItems,
                    onIntent = onIntent
                )
//            }
        }
    }
}