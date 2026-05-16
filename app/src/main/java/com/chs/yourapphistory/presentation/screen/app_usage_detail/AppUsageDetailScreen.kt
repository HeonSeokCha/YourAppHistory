package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.chs.yourapphistory.common.toConvertDisplayYearDate
import com.chs.yourapphistory.common.toDisplayYearDate
import com.chs.yourapphistory.domain.model.UsageEventType
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
    dailyPagingItems: LazyPagingItems<Map<UsageEventType, List<Pair<Int, Int>>>>,
    weeklyPagingItems: LazyPagingItems<Map<UsageEventType, List<Pair<LocalDate, Int>>>>,
    onIntent: (AppUsageDetailIntent) -> Unit
) {
    /* date related variables */
    val datePagerState = rememberPagerState(
        pageCount = { state.dateList.count() },
        initialPage = state.dateIdx.first
    )

    LaunchedEffect(dailyPagingItems.loadState.refresh) {
        when (dailyPagingItems.loadState.refresh) {
            is LoadState.Loading -> onIntent(AppUsageDetailIntent.DateLoading)
            is LoadState.NotLoading -> onIntent(AppUsageDetailIntent.DateLoadComplete)
            is LoadState.Error -> onIntent(AppUsageDetailIntent.Error)
        }
    }

    LaunchedEffect(datePagerState.currentPage, datePagerState.isScrollInProgress) {
        if (state.dateList.isEmpty()) return@LaunchedEffect
        if (datePagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect
        if (datePagerState.isScrollInProgress) return@LaunchedEffect

        onIntent(
            AppUsageDetailIntent.OnChangeTargetDateIdx(datePagerState.currentPage to state.dateIdx.second)
        )
    }

    LaunchedEffect(state.dateIdx) {
        if (state.dateList.isEmpty()) return@LaunchedEffect
        val page = state.dateIdx.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            (this.first * 7 + this.second) - initIdx
        }
        if (datePagerState.currentPage != state.dateIdx.first) {
            datePagerState.scrollToPage(state.dateIdx.first)
        }
        if (state.isDateLoading) return@LaunchedEffect
        onIntent(AppUsageDetailIntent.OnChangeDateCurrentPage(page))
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
        } else {
            ItemDateList(
                state = datePagerState,
                minDate = state.minDate,
                targetDate = state.displayDate,
                item = state.dateList,
                onClick = { onIntent(AppUsageDetailIntent.OnChangeTargetDateIdx(it)) }
            )

            ItemDailyPagingInfo(
                state = state,
                dailyPagingItems = dailyPagingItems,
                onIntent = onIntent
            )
        }
    }
}