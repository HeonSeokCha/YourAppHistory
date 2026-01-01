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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.toConvertDisplayYearDate
import com.chs.yourapphistory.common.toDisplayYearDate
import com.chs.yourapphistory.domain.model.SortType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDate
import kotlin.times

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
    dailyPagingItems: LazyPagingItems<Map<SortType, List<Pair<Int, Int>>>>,
    weeklyPagingItems: LazyPagingItems<Map<SortType, List<Pair<LocalDate, Int>>>>,
    onIntent: (AppUsageDetailIntent) -> Unit
) {
    /* date related variables */
    val datePagerState = if (state.dateList.isNotEmpty()) {
        rememberPagerState(
            pageCount = { state.dateList.count() },
            initialPage = state.dateIdx.first
        )
    } else {
        rememberPagerState(pageCount = { 0 })
    }

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
        val page = state.dateIdx.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            (this.first * 7 + this.second) - initIdx
        }
        if (datePagerState.currentPage != state.dateIdx.first) {
            datePagerState.scrollToPage(state.dateIdx.first)
        }

        onIntent(AppUsageDetailIntent.OnChangeDateCurrentPage(page))
    }
    /* date related variables end*/

    /* week related variables */
    val weekPagerState = if (state.weekList.isNotEmpty()) {
        rememberPagerState(pageCount = { state.weekList.count() })
    } else {
        rememberPagerState(pageCount = { 0 })
    }

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(0.7f),
                text = if (state.isWeeklyMode) {
                    state.displayWeek.toDisplayYearDate()
                } else {
                    state.displayDate.toConvertDisplayYearDate()
                },
                fontSize = 16.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(8.dp).weight(0.1f))

            Text(
                modifier = Modifier
                    .weight(0.2f)
                    .clickable { onIntent(AppUsageDetailIntent.OnChangeViewType) },
                text = if (state.isWeeklyMode) {
                    "일별 보기"
                } else {
                    "주별 보기"
                },
                fontSize = 16.sp
            )
        }

        if (state.isWeeklyMode) {
            ItemWeekList(
                state = weekPagerState,
                targetWeek = state.displayWeek,
                item = state.weekList,
                onIntent = onIntent
            )
        } else {
            ItemDateList(
                state = datePagerState,
                minDate = state.minDate,
                targetDate = state.displayDate,
                item = state.dateList,
                onIntent = onIntent
            )
        }

        if (state.isWeeklyMode) {
            ItemWeeklyPagingInfo(
                state = state,
                weeklyPagingItems = weeklyPagingItems,
                onIntent = onIntent
            )
        } else {
            ItemDailyPagingInfo(
                state = state,
                dailyPagingItems = dailyPagingItems,
                onIntent = onIntent
            )
        }
    }
}