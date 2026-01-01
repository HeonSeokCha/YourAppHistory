package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.toCalcDailyCount
import com.chs.yourapphistory.common.toCalcDailyUsage
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.presentation.screen.common.DailyUsageChart
import com.chs.yourapphistory.presentation.screen.common.WeeklyUsageChart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDate
import kotlin.collections.sumOf

@Composable
fun ItemDailyPagingInfo(
    state: AppUsageDetailState,
    dailyPagingItems: LazyPagingItems<Map<SortType, List<Pair<Int, Int>>>>,
    onIntent: (AppUsageDetailIntent) -> Unit
) {
    val scrollState = rememberScrollState()

    val dailyUsagePager = if (state.isDateLoading) {
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

    val dailyForegroundUsagePager = if (state.isDateLoading) {
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

    val dailyNotifyPager = if (state.isDateLoading) {
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

    val dailyLaunchPager = if (state.isDateLoading) {
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
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        if (dailyUsagePager.isScrollInProgress) return@LaunchedEffect
        val idx = dailyUsagePager.currentPage.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            ((initIdx + this) / 7) to (initIdx + this) % 7
        }
        onIntent(AppUsageDetailIntent.OnChangeTargetDateIdx(idx))
    }

    LaunchedEffect(
        dailyForegroundUsagePager.currentPage,
        dailyForegroundUsagePager.isScrollInProgress
    ) {
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        if (dailyForegroundUsagePager.isScrollInProgress) return@LaunchedEffect
        val idx = dailyForegroundUsagePager.currentPage.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            ((initIdx + this) / 7) to (initIdx + this) % 7
        }
        onIntent(AppUsageDetailIntent.OnChangeTargetDateIdx(idx))
    }

    LaunchedEffect(dailyNotifyPager.currentPage, dailyNotifyPager.isScrollInProgress) {
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        if (dailyNotifyPager.isScrollInProgress) return@LaunchedEffect
        val idx = dailyNotifyPager.currentPage.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            ((initIdx + this) / 7) to (initIdx + this) % 7
        }
        onIntent(AppUsageDetailIntent.OnChangeTargetDateIdx(idx))
    }

    LaunchedEffect(dailyLaunchPager.currentPage, dailyLaunchPager.isScrollInProgress) {
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        if (dailyLaunchPager.isScrollInProgress) return@LaunchedEffect
        val idx = dailyLaunchPager.currentPage.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            ((initIdx + this) / 7) to (initIdx + this) % 7
        }
        onIntent(AppUsageDetailIntent.OnChangeTargetDateIdx(idx))
    }

    LaunchedEffect(state.dateCurrentPage) {
        awaitAll(
            async { dailyForegroundUsagePager.scrollToPage(state.dateCurrentPage) },
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
                DailyUsageChart(
                    title = item.sumOf { it.second }.convertToRealUsageTime(),
                    subTitle = "${state.packageLabel} 사용 시간",
                    list = item,
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
            val item = dailyPagingItems[it]?.get(SortType.ForegroundUsageEvent)
            if (item != null) {
                DailyUsageChart(
                    title = item.sumOf { it.second }.convertToRealUsageTime(),
                    subTitle = "포그라운드 실행 시간 ",
                    list = item,
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
            val item = dailyPagingItems[it]?.get(SortType.NotifyEvent)
            if (item != null) {
                DailyUsageChart(
                    title = "알림 ${item.sumOf { it.second }}개",
                    list = item,
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
            val item = dailyPagingItems[it]?.get(SortType.LaunchEvent)
            if (item != null) {
                DailyUsageChart(
                    title = "총 실행 횟수 ${item.sumOf { it.second }}회",
                    list = item,
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
    weeklyPagingItems: LazyPagingItems<Map<SortType, List<Pair<LocalDate, Int>>>>,
    onIntent: (AppUsageDetailIntent) -> Unit
) {
    val scrollState = rememberScrollState()


    val weeklyUsagePager =
        rememberPagerState(initialPage = 0, pageCount = { weeklyPagingItems.itemCount })

    val weeklyForegroundUsagePager =
        rememberPagerState(initialPage = 0, pageCount = { weeklyPagingItems.itemCount })

    val weeklyNotifyPager =
        rememberPagerState(initialPage = 0, pageCount = { weeklyPagingItems.itemCount })

    val weeklyLaunchPager =
        rememberPagerState(initialPage = 0, pageCount = { weeklyPagingItems.itemCount })

    LaunchedEffect(state.weekCurrentPage) {
        awaitAll(
            async { weeklyUsagePager.scrollToPage(state.weekCurrentPage) },
            async { weeklyForegroundUsagePager.scrollToPage(state.weekCurrentPage) },
            async { weeklyNotifyPager.scrollToPage(state.weekCurrentPage) },
            async { weeklyLaunchPager.scrollToPage(state.weekCurrentPage) }
        )
    }

    LaunchedEffect(weeklyUsagePager.currentPage, weeklyUsagePager.isScrollInProgress) {
        if (state.weekList.isEmpty() || state.isWeekLoading) return@LaunchedEffect
        if (weeklyUsagePager.isScrollInProgress) return@LaunchedEffect
        if (weeklyUsagePager.currentPage == state.weekIdx.run { (this.first * 5) + this.second }) return@LaunchedEffect
        val idx = weeklyUsagePager.run { (this.currentPage / 5) to (this.currentPage % 5) }
        onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(idx))
    }

    LaunchedEffect(
        weeklyForegroundUsagePager.currentPage,
        weeklyForegroundUsagePager.isScrollInProgress
    ) {
        if (state.weekList.isEmpty() || state.isWeekLoading) return@LaunchedEffect
        if (weeklyForegroundUsagePager.isScrollInProgress) return@LaunchedEffect
        val idx = weeklyForegroundUsagePager.run { (this.currentPage / 5) to (this.currentPage % 5) }
        onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(idx))
    }

    LaunchedEffect(weeklyNotifyPager.currentPage, weeklyNotifyPager.isScrollInProgress) {
        if (state.weekList.isEmpty() || state.isWeekLoading) return@LaunchedEffect
        if (weeklyNotifyPager.isScrollInProgress) return@LaunchedEffect
        val idx = weeklyNotifyPager.run { (this.currentPage / 5) to (this.currentPage % 5) }
        onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(idx))
    }

    LaunchedEffect(weeklyLaunchPager.currentPage, weeklyLaunchPager.isScrollInProgress) {
        if (state.weekList.isEmpty() || state.isWeekLoading) return@LaunchedEffect
        if (weeklyLaunchPager.isScrollInProgress) return@LaunchedEffect
        val idx = weeklyLaunchPager.run { (this.currentPage / 5) to (this.currentPage % 5) }
        onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(idx))
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
            val item = weeklyPagingItems[it]?.get(SortType.UsageEvent)
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
            val item = weeklyPagingItems[it]?.get(SortType.ForegroundUsageEvent)
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
            val item = weeklyPagingItems[it]?.get(SortType.NotifyEvent)
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
            val item = weeklyPagingItems[it]?.get(SortType.LaunchEvent)
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
                    convertText = { "${it}번" }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}