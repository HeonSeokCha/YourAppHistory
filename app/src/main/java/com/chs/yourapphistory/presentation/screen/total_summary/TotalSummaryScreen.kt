package com.chs.yourapphistory.presentation.screen.total_summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.presentation.screen.app_usage_detail.ItemDateList
import com.chs.yourapphistory.presentation.screen.common.placeholder
import java.time.LocalDate

@Composable
fun TotalSummaryScreenRoot(
    viewModel: TotalSummaryViewModel,
    onNavigateUsageDetail: (String, String, Long) -> Unit,
    onNavigateUsedAppList: (Long, SortType) -> Unit
) {
    val state: TotalSummaryState by viewModel.state.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingList.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TotalSummaryEffect.NavigateUsageDetail -> {
                    onNavigateUsageDetail(
                        effect.packageName,
                        effect.label,
                        effect.targetDate
                    )
                }

                is TotalSummaryEffect.NavigateUsedAppList -> {
                    onNavigateUsedAppList(
                        effect.targetDate,
                        effect.sortType
                    )
                }
            }
        }
    }

    TotalSummaryScreen(
        state = state,
        pagingItems = pagingItems,
        onIntent = viewModel::handleIntent
    )
}

@Composable
fun TotalSummaryScreen(
    state: TotalSummaryState,
    pagingItems: LazyPagingItems<Map<SortType, List<Pair<LocalDate, List<AppTotalUsageInfo>>>>>,
    onIntent: (TotalSummaryIntent) -> Unit
) {
    val datePagerState = if (state.dateList.isNotEmpty()) {
        rememberPagerState(pageCount = { state.dateList.count() })
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    LaunchedEffect(pagingItems.loadState.refresh) {
        when (pagingItems.loadState.refresh) {
            is LoadState.Loading -> onIntent(TotalSummaryIntent.Loading)
            is LoadState.NotLoading -> onIntent(TotalSummaryIntent.LoadComplete)
            is LoadState.Error -> onIntent(TotalSummaryIntent.Error)
        }
    }

    LaunchedEffect(datePagerState.currentPage, datePagerState.isScrollInProgress) {
        if (state.dateList.isEmpty()) return@LaunchedEffect
        if (datePagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect
        if (datePagerState.isScrollInProgress) return@LaunchedEffect
        onIntent(TotalSummaryIntent.OnChangeDateCurrentPage(datePagerState.currentPage))
    }

    LaunchedEffect(state.dateCurrentPage) {
        datePagerState.scrollToPage(state.dateCurrentPage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier
                    .placeholder(
                        visible = state.loading,
                        shape = RoundedCornerShape(5.dp)
                    ),
                text = state.displayDate.toConvertDisplayYearDate(),
                fontSize = 16.sp,
                maxLines = 2
            )
        }

        if (state.loading) {
            ItemLoadingFromTotal()
        } else {
            ItemDateList(
                state = datePagerState,
                minDate = state.minDate,
                targetDate = state.displayDate,
                item = state.dateList,
                onClick = {
                    onIntent(TotalSummaryIntent.OnChangeTargetDateIdx(it))
                }
            )

            ItemTotalPaging(
                state = state,
                dailyPagingItems = pagingItems,
                onIntent = onIntent
            )
        }
    }
}

@Composable
private fun ItemLoadingFromTotal() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(7) {
            Text(
                text = it.toString(),
                modifier = Modifier
                    .placeholder(
                        visible = true,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .placeholder(
                visible = true,
                shape = RoundedCornerShape(4.dp)
            )
    )
}