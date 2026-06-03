package com.chs.yourapphistory.presentation.screen.app_usage_detail

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertToRealUsageHour
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.toCalcDailyCount
import com.chs.yourapphistory.common.toCalcDailyUsage
import com.chs.yourapphistory.domain.model.UsageEventType
import com.chs.yourapphistory.presentation.screen.common.DailyUsageChart
import com.chs.yourapphistory.presentation.screen.common.WeeklyUsageChart
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.collections.sumOf

@Composable
fun ItemDailyPagingInfo(
    state: AppUsageDetailState,
    dailyPagingItems: LazyPagingItems<Pair<LocalDate, Map<UsageEventType, List<Pair<Int, Int>>>>>,
    onIntent: (AppUsageDetailIntent) -> Unit
) {
    val scrollState = rememberScrollState()

    val dailyUsagePager = rememberPagerState(pageCount = { dailyPagingItems.itemCount })

    val dailyForegroundUsagePager = rememberPagerState(pageCount = { dailyPagingItems.itemCount })

    val dailyNotifyPager = rememberPagerState(pageCount = { dailyPagingItems.itemCount })

    val dailyLaunchPager = rememberPagerState(pageCount = { dailyPagingItems.itemCount })


    val allPagerStates = listOf(
        dailyUsagePager,
        dailyForegroundUsagePager,
        dailyNotifyPager,
        dailyLaunchPager
    )

    allPagerStates.forEach { pagerState ->
        LaunchedEffect(
            pagerState.currentPage,
            pagerState.isScrollInProgress
        ) {
            if (state.isDateLoading) return@LaunchedEffect
            if (dailyPagingItems.itemCount == 0) return@LaunchedEffect
            if (pagerState.isScrollInProgress) return@LaunchedEffect
            val newPage = pagerState.currentPage

            onIntent(AppUsageDetailIntent.OnChangeDate(dailyPagingItems[pagerState.currentPage]!!.first))

            allPagerStates
                .filter { it !== pagerState }
                .forEach { other ->
                    if (other.currentPage == newPage) return@forEach
                    launch { other.scrollToPage(newPage) }
                }
        }
    }

    LaunchedEffect(state.dateCurrentPage) {
        dailyPagingItems.itemCount
        allPagerStates.forEach { pagerState ->
            launch { pagerState.scrollToPage(state.dateCurrentPage) }
        }
    }

    LaunchedEffect(state.dailyPagerPageIdx) {
        if (dailyPagingItems.loadState.refresh !is LoadState.NotLoading) return@LaunchedEffect
        chsLog("state.dailyPagerPageIdx ${state.dailyPagerPageIdx}")
        allPagerStates.forEach { pagerState ->
            launch { pagerState.scrollToPage(state.dailyPagerPageIdx) }
        }
    }

    LaunchedEffect(state.dateIdx) {
        val idx = state.dateIdx.first * 7 + state.dateIdx.second
        allPagerStates.forEach { pagerState ->
            launch { pagerState.scrollToPage(idx) }
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
            val item = dailyPagingItems[it]?.second?.get(UsageEventType.UsageEvent)
            if (item != null) {
                DailyUsageChart(
                    title = item.sumOf { it.second }.convertToRealUsageTime(),
                    subTitle = "${state.packageLabel} 사용 시간",
                    list = item,
                    usageEventType = UsageEventType.UsageEvent,
                    convertText = { it.convertToRealUsageMinutes() }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            pageSpacing = 8.dp,
            state = dailyForegroundUsagePager,
            reverseLayout = true,
            key = { it }
        ) {
            val item = dailyPagingItems[it]?.second?.get(UsageEventType.ForegroundUsageEvent)
            if (item != null) {
                DailyUsageChart(
                    title = item.sumOf { it.second }.convertToRealUsageTime(),
                    subTitle = "포그라운드 실행 시간 ",
                    list = item,
                    usageEventType = UsageEventType.ForegroundUsageEvent,
                    convertText = { it.convertToRealUsageMinutes() }
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
            val item = dailyPagingItems[it]?.second?.get(UsageEventType.NotifyEvent)
            if (item != null) {
                DailyUsageChart(
                    title = "알림 ${item.sumOf { it.second }}개",
                    list = item,
                    usageEventType = UsageEventType.NotifyEvent,
                    convertText = { "${it}개" }
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
            val item = dailyPagingItems[it]?.second?.get(UsageEventType.LaunchEvent)
            if (item != null) {
                DailyUsageChart(
                    title = "총 실행 횟수 ${item.sumOf { it.second }}회",
                    list = item,
                    usageEventType = UsageEventType.LaunchEvent,
                    convertText = { "${it}회" }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ItemWeeklyPagingInfo(
    state: AppUsageDetailState,
    weeklyPagingItems: LazyPagingItems<Map<UsageEventType, List<Pair<LocalDate, Int>>>>,
    onIntent: (AppUsageDetailIntent) -> Unit
) {
    val scrollState = rememberScrollState()

    val weeklyUsagePager = rememberPagerState(
        initialPage = state.weekCurrentPage,
        pageCount = { weeklyPagingItems.itemCount }
    )

    val weeklyForegroundUsagePager = rememberPagerState(
        initialPage = state.weekCurrentPage,
        pageCount = { weeklyPagingItems.itemCount }
    )

    val weeklyNotifyPager = rememberPagerState(
        initialPage = state.weekCurrentPage,
        pageCount = { weeklyPagingItems.itemCount }
    )

    val weeklyLaunchPager = rememberPagerState(
        initialPage = state.weekCurrentPage,
        pageCount = { weeklyPagingItems.itemCount }
    )

    val allPagerStates = listOf(
        weeklyUsagePager,
        weeklyForegroundUsagePager,
        weeklyNotifyPager,
        weeklyLaunchPager
    )

    allPagerStates.forEach { pagerState ->
        LaunchedEffect(
            pagerState.currentPage,
            pagerState.isScrollInProgress
        ) {
            if (pagerState.isScrollInProgress) return@LaunchedEffect
            val newPage = pagerState.currentPage

            if (state.weekCurrentPage != newPage) {
                val idx = pagerState.run { (newPage / 5) to (newPage % 5) }
                onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(idx))
            }

            allPagerStates
                .filter { it !== pagerState }
                .forEach { other ->
                    if (other.currentPage == newPage) return@forEach
                    launch { other.scrollToPage(newPage) }
                }
        }
    }

    LaunchedEffect(state.weekCurrentPage) {
        allPagerStates.forEach { pagerState ->
            launch { pagerState.scrollToPage(state.weekCurrentPage) }
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
            state = weeklyUsagePager,
            reverseLayout = true,
            key = { it }
        ) {
            val item = weeklyPagingItems[it]?.get(UsageEventType.UsageEvent)
            if (item != null) {
                WeeklyUsageChart(
                    title = "${item.toCalcDailyUsage()}/일",
                    subTitle = buildAnnotatedString {
                        append("이번 주 총 ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(item.sumOf { it.second }.convertToRealUsageHour())
                        }
                    },
                    list = item,
                    usageEventType = UsageEventType.UsageEvent,
                    convertText = { it.convertToRealUsageHour() }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            pageSpacing = 8.dp,
            state = weeklyForegroundUsagePager,
            reverseLayout = true,
            key = { it }
        ) {
            val item = weeklyPagingItems[it]?.get(UsageEventType.ForegroundUsageEvent)
            if (item != null) {
                WeeklyUsageChart(
                    title = "${
                        item.toCalcDailyUsage()
                    }/일",
                    subTitle = buildAnnotatedString {
                        append("이번 주 총 ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(item.sumOf { it.second }.convertToRealUsageHour())
                        }
                    },
                    list = item,
                    usageEventType = UsageEventType.ForegroundUsageEvent,
                    convertText = { it.convertToRealUsageHour() }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            pageSpacing = 8.dp,
            state = weeklyNotifyPager,
            reverseLayout = true,
            key = { it }
        ) {
            val item = weeklyPagingItems[it]?.get(UsageEventType.NotifyEvent)
            if (item != null) {
                WeeklyUsageChart(
                    title = "알림 ${item.toCalcDailyCount()}개/일",
                    subTitle = buildAnnotatedString {
                        append("이번 주 총 알림 ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(item.sumOf { it.second }.toString())
                        }
                        append("개")
                    },
                    list = item,
                    usageEventType = UsageEventType.NotifyEvent,
                    convertText = { "${it}개" }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            pageSpacing = 8.dp,
            state = weeklyLaunchPager,
            reverseLayout = true,
            key = { it }
        ) {
            val item = weeklyPagingItems[it]?.get(UsageEventType.LaunchEvent)
            if (item != null) {
                WeeklyUsageChart(
                    title = "앱 실행 ${item.toCalcDailyCount()}회/일",
                    subTitle = buildAnnotatedString {
                        append("이번 주 총 앱 실행 ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(item.sumOf { it.second }.toString())
                        }
                        append("회")
                    },
                    list = item,
                    usageEventType = UsageEventType.LaunchEvent,
                    convertText = { "${it}번" }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}