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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.chs.yourapphistory.common.convertToRealUsageHour
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import com.chs.yourapphistory.domain.model.UsageEventType
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailIntent
import com.chs.yourapphistory.presentation.screen.common.WeeklyColorUsageChart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ItemTotalPaging(
    state: TotalSummaryState,
    dailyPagingItems: LazyPagingItems<Map<UsageEventType, List<Pair<LocalDate, List<AppTotalUsageInfo>>>>>,
    onIntent: (TotalSummaryIntent) -> Unit
) {
    val scrollState = rememberScrollState()

    val dailyUsagePager = rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })

    val dailyNotifyPager = rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })

    val dailyLaunchPager = rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })

    val allPagerStates = listOf(
        dailyUsagePager,
        dailyNotifyPager,
        dailyLaunchPager
    )

    allPagerStates.forEach { pagerState ->
        LaunchedEffect(
            pagerState.currentPage,
            pagerState.isScrollInProgress
        ) {
            if (state.loading) return@LaunchedEffect
            if (dailyPagingItems.itemCount == 0) return@LaunchedEffect
            if (pagerState.isScrollInProgress) return@LaunchedEffect
            val newPage = pagerState.currentPage

            if (state.dateCurrentPage != newPage) {
                onIntent(TotalSummaryIntent.OnChangeDateCurrentPage(pagerState.currentPage))
            }

            allPagerStates
                .filter { it !== pagerState }
                .forEach { other ->
                    if (other.currentPage == newPage) return@forEach
                    launch { other.scrollToPage(newPage) }
                }
        }
    }

    LaunchedEffect(state.dateCurrentPage) {
        allPagerStates.forEach { pagerState ->
            launch { pagerState.scrollToPage(state.dateCurrentPage) }
        }
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
            val item = dailyPagingItems[it]?.get(UsageEventType.UsageEvent)
            if (item != null) {
                val currentIdx = item.size - 1 - state.dateIdx.second
                WeeklyColorUsageChart(
                    title = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(item[currentIdx].second.sumOf { it.totalUsedInfo.toInt() }.convertToRealUsageHour())
                        }
                    },
                    subTitle = "총 실제 실행 시간",
                    list = item,
                    usageType = UsageEventType.UsageEvent,
                    selectIdx = currentIdx,
                    onBarClick = {
                        onIntent(TotalSummaryIntent.OnChangeTargetDateIdx(state.dateIdx.first to item.size - 1 - it))
                    },
                    onPackageClick = { packageName, label ->
                        onIntent(
                            TotalSummaryIntent.ClickPackageName(
                                packageName = packageName,
                                label = label,
                                targetDate = item[currentIdx].first.toMillis()
                            )
                        )
                    },
                    onButtonClick = {
                        onIntent(
                            TotalSummaryIntent.ClickUsedAppList(
                                targetDate = item[currentIdx].first.toMillis(),
                                usageEventType = UsageEventType.UsageEvent
                            )
                        )
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
            val item = dailyPagingItems[it]?.get(UsageEventType.NotifyEvent)
            if (item != null) {
                val currentIdx = item.size - 1 - state.dateIdx.second
                WeeklyColorUsageChart(
                    title = buildAnnotatedString {
                        append("알림 ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(item[currentIdx].second.sumOf { it.totalUsedInfo }.toString())
                        }
                        append("개")
                    },
                    list = item,
                    usageType = UsageEventType.NotifyEvent,
                    selectIdx = currentIdx,
                    onBarClick = {
                        onIntent(TotalSummaryIntent.OnChangeTargetDateIdx(state.dateIdx.first to item.size - 1 - it))
                    },
                    onPackageClick = { packageName, label ->
                        onIntent(
                            TotalSummaryIntent.ClickPackageName(
                                packageName = packageName,
                                label = label,
                                targetDate = item[currentIdx].first.toMillis()
                            )
                        )
                    },
                    onButtonClick = {
                        onIntent(
                            TotalSummaryIntent.ClickUsedAppList(
                                targetDate = item[currentIdx].first.toMillis(),
                                usageEventType = UsageEventType.NotifyEvent
                            )
                        )
                    }
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
            val item = dailyPagingItems[it]?.get(UsageEventType.LaunchEvent)
            if (item != null) {
                val currentIdx = item.size - 1 - state.dateIdx.second
                WeeklyColorUsageChart(
                    title = buildAnnotatedString {
                        append("앱 실행횟수 ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(item[currentIdx].second.sumOf { it.totalUsedInfo }.toString())
                        }
                        append("번")
                    },
                    list = item,
                    usageType = UsageEventType.LaunchEvent,
                    selectIdx = currentIdx,
                    onBarClick = {
                        onIntent(TotalSummaryIntent.OnChangeTargetDateIdx(state.dateIdx.first to item.size - 1 - it))
                    },
                    onPackageClick = { packageName, label ->
                        onIntent(
                            TotalSummaryIntent.ClickPackageName(
                                packageName = packageName,
                                label = label,
                                targetDate = item[currentIdx].first.toMillis()
                            )
                        )
                    },
                    onButtonClick = {
                        onIntent(
                            TotalSummaryIntent.ClickUsedAppList(
                                targetDate = item[currentIdx].first.toMillis(),
                                usageEventType = UsageEventType.LaunchEvent
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}