package com.chs.yourapphistory.presentation.screen.total_summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertToRealUsageHour
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.presentation.screen.common.WeeklyColorUsageChart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDate

@Composable
fun ItemTotalPaging(
    state: TotalSummaryState,
    dailyPagingItems: LazyPagingItems<Map<SortType, List<Pair<LocalDate, List<AppTotalUsageInfo>>>>>,
    onIntent: (TotalSummaryIntent) -> Unit
) {
    val scrollState = rememberScrollState()

    val dailyUsagePager = if (state.dateList.isNotEmpty()) {
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
    } else {
        val initIdx = state.dateList.flatten().run {
            this.indexOf(state.displayDate) - this.indexOf(LocalDate.now())
        }
        if (initIdx > dailyPagingItems.itemCount) {
            rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
        } else {
            rememberPagerState(initialPage = initIdx, pageCount = { dailyPagingItems.itemCount })
        }
    }

    val dailyNotifyPager = if (state.dateList.isNotEmpty()) {
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
    } else {
        val initIdx = state.dateList.flatten().run {
            this.indexOf(state.displayDate) - this.indexOf(LocalDate.now())
        }
        if (initIdx > dailyPagingItems.itemCount) {
            rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
        } else {
            rememberPagerState(initialPage = initIdx, pageCount = { dailyPagingItems.itemCount })
        }
    }

    val dailyLaunchPager = if (state.dateList.isNotEmpty()) {
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
    } else {
        val initIdx = state.dateList.flatten().run {
            this.indexOf(state.displayDate) - this.indexOf(LocalDate.now())
        }
        if (initIdx > dailyPagingItems.itemCount) {
            rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
        } else {
            rememberPagerState(initialPage = initIdx, pageCount = { dailyPagingItems.itemCount })
        }
    }

    LaunchedEffect(dailyUsagePager.currentPage, dailyUsagePager.isScrollInProgress) {
        if (state.dateList.isEmpty()) return@LaunchedEffect
        if (dailyUsagePager.isScrollInProgress) return@LaunchedEffect
        onIntent(TotalSummaryIntent.OnChangeDateCurrentPage(dailyUsagePager.currentPage))
    }

    LaunchedEffect(dailyNotifyPager.currentPage, dailyNotifyPager.isScrollInProgress) {
        if (state.dateList.isEmpty()) return@LaunchedEffect
        if (dailyNotifyPager.isScrollInProgress) return@LaunchedEffect
        onIntent(TotalSummaryIntent.OnChangeDateCurrentPage(dailyNotifyPager.currentPage))
    }

    LaunchedEffect(dailyLaunchPager.currentPage, dailyLaunchPager.isScrollInProgress) {
        if (state.dateList.isEmpty()) return@LaunchedEffect
        if (dailyLaunchPager.isScrollInProgress) return@LaunchedEffect
        onIntent(TotalSummaryIntent.OnChangeDateCurrentPage(dailyLaunchPager.currentPage))
    }

    LaunchedEffect(state.dateCurrentPage) {
        awaitAll(
            async { dailyUsagePager.scrollToPage(state.dateCurrentPage) },
            async { dailyNotifyPager.scrollToPage(state.dateCurrentPage) },
            async { dailyLaunchPager.scrollToPage(state.dateCurrentPage) }
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState),
    ) {
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            pageSpacing = 8.dp,
            state = dailyUsagePager,
            reverseLayout = true,
            key = { it }
        ) {
            val item = dailyPagingItems[it]?.get(SortType.UsageEvent)
            if (item != null) {
                chsLog("IDX : ${state.dateIdx.second}")
                val a = item.size - 1 - state.dateIdx.second
                WeeklyColorUsageChart(
                    title = item[a].second.sumOf { it.totalUsedInfo.toInt() }.convertToRealUsageHour(),
                    subTitle = "총 실제 실행 시간",
                    list = item,
                    onClick = {
                        chsLog("ONCLICK : $it")
                        onIntent(TotalSummaryIntent.OnChangeTargetDateIdx(0 to item.size - 1 - it))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            pageSpacing = 8.dp,
            state = dailyNotifyPager,
            reverseLayout = true,
            key = { it }
        ) {
            val item = dailyPagingItems[it]?.get(SortType.NotifyEvent)
            if (item != null) {
                WeeklyColorUsageChart(
                    title = "${item[it].second.sumOf { it.totalUsedInfo}} 회",
                    subTitle = "총 알림 횟수",
                    list = item,
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            pageSpacing = 8.dp,
            state = dailyLaunchPager,
            reverseLayout = true,
            key = { it }
        ) {
            val item = dailyPagingItems[it]?.get(SortType.LaunchEvent)
            if (item != null) {
                WeeklyColorUsageChart(
                    title = "${item[it].second.sumOf { it.totalUsedInfo}} 번",
                    subTitle = "총 실행 횟수",
                    list = item,
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}