package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import java.time.LocalDate
import kotlin.collections.sumOf

@Composable
fun ItemDailyPagingInfo(
    packageLabel: String,
    dailyUsagePager: PagerState,
    dailyForegroundUsagePager: PagerState,
    dailyNotifyPager: PagerState,
    dailyLaunchPager: PagerState,
    dailyPagingItems: LazyPagingItems<Map<SortType, List<Pair<Int, Int>>>>
) {
    val scrollState = rememberScrollState()
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
                    subTitle = "$packageLabel 사용 시간",
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
    weeklyUsagePager: PagerState,
    weeklyForegroundUsagePager: PagerState,
    weeklyNotifyPager: PagerState,
    weeklyLaunchPager: PagerState,
    weeklyPagingItems: LazyPagingItems<Map<SortType, List<Pair<LocalDate, Int>>>>
) {
    val scrollState = rememberScrollState()
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