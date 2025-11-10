package com.chs.yourapphistory.presentation.screen.app_usage_detail

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chs.yourapphistory.common.convertToRealUsageHour
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.getYearOfWeek
import com.chs.yourapphistory.common.toCalcDailyCount
import com.chs.yourapphistory.common.toCalcDailyUsage
import com.chs.yourapphistory.common.toConvertDisplayYearDate
import com.chs.yourapphistory.common.toDisplayYearDate
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.presentation.screen.common.DailyUsageChart
import com.chs.yourapphistory.presentation.screen.common.ItemPullToRefreshBox
import com.chs.yourapphistory.presentation.screen.common.WeeklyUsageChart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AppUsageDetailScreenRoot(
    viewModel: AppUsageDetailViewModel,
    onBack: () -> Unit
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
    val coroutineScope = rememberCoroutineScope()
    val datePagerState = if (state.dateList.isNotEmpty()) {
        rememberPagerState(pageCount = { state.dateList.count() })
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    LaunchedEffect(datePagerState.currentPage, datePagerState.isScrollInProgress) {
        if (state.dateList.isEmpty()) return@LaunchedEffect
        if (!datePagerState.isScrollInProgress) {
            onIntent(
                AppUsageDetailIntent.OnChangeTargetDateIdx(datePagerState.currentPage to state.dateIdx.second)
            )
        }
    }

    val dailyUsagePager =
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })

    val dailyForegroundUsagePager =
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })

    val dailyNotifyPager =
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })

    val dailyLaunchPager =
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })

    LaunchedEffect(dailyUsagePager.currentPage, dailyUsagePager.isScrollInProgress) {
        if (!dailyUsagePager.isScrollInProgress) {
            coroutineScope.launch {
                dailyForegroundUsagePager.animateScrollToPage(dailyUsagePager.currentPage)
            }
            coroutineScope.launch {
                dailyNotifyPager.animateScrollToPage(dailyUsagePager.currentPage)
            }
            coroutineScope.launch {
                dailyLaunchPager.animateScrollToPage(dailyUsagePager.currentPage)
            }
        }
    }

    LaunchedEffect(
        dailyForegroundUsagePager.currentPage,
        dailyForegroundUsagePager.isScrollInProgress
    ) {
        if (!dailyForegroundUsagePager.isScrollInProgress) {
            coroutineScope.launch {
                dailyUsagePager.animateScrollToPage(dailyForegroundUsagePager.currentPage)
            }
            coroutineScope.launch {
                dailyNotifyPager.animateScrollToPage(dailyForegroundUsagePager.currentPage)
            }
            coroutineScope.launch {
                dailyLaunchPager.animateScrollToPage(dailyForegroundUsagePager.currentPage)
            }
        }
    }

    LaunchedEffect(dailyNotifyPager.currentPage, dailyNotifyPager.isScrollInProgress) {
        if (!dailyNotifyPager.isScrollInProgress) {
            coroutineScope.launch {
                dailyUsagePager.animateScrollToPage(dailyNotifyPager.currentPage)
            }
            coroutineScope.launch {
                dailyForegroundUsagePager.animateScrollToPage(dailyNotifyPager.currentPage)
            }
            coroutineScope.launch {
                dailyLaunchPager.animateScrollToPage(dailyNotifyPager.currentPage)
            }
        }
    }

    LaunchedEffect(dailyLaunchPager.currentPage, dailyLaunchPager.isScrollInProgress) {
        if (!dailyLaunchPager.isScrollInProgress) {
            coroutineScope.launch {
                dailyUsagePager.animateScrollToPage(dailyLaunchPager.currentPage)
            }
            coroutineScope.launch {
                dailyForegroundUsagePager.animateScrollToPage(dailyLaunchPager.currentPage)
            }
            coroutineScope.launch {
                dailyNotifyPager.animateScrollToPage(dailyLaunchPager.currentPage)
            }
        }
    }

    val weekPagerState = if (state.weekList.isNotEmpty()) {
        rememberPagerState(pageCount = { state.weekList.count() })
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (state.isWeeklyMode) {
                Text(
                    text = state.displayWeek.toDisplayYearDate(),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            } else {
                Text(
                    text = if (state.displayDate == LocalDate.now()) {
                        "오늘"
                    } else {
                        state.displayDate.toConvertDisplayYearDate()
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }

            Text(
                modifier = Modifier
                    .clickable { onIntent(AppUsageDetailIntent.OnChangeViewType) },
                text = if (state.isWeeklyMode) {
                    "주별 보기"
                } else {
                    "일별 보기"
                },
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }


        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            state = datePagerState,
            reverseLayout = true,
            key = { state.dateList[it] }
        ) {
            val item = state.dateList[it]
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                item.reversed().forEachIndexed { idx, date ->
                    Text(
                        modifier = Modifier
                            .clickable {
                                if (date >= state.minDate && date <= LocalDate.now()) {
                                    onIntent(
                                        AppUsageDetailIntent.OnChangeTargetDateIdx(
                                            datePagerState.currentPage to item.size - 1 - idx
                                        )
                                    )
                                }
                            }
                            .drawBehind {
                                drawRoundRect(
                                    color = if (state.displayDate == date) Color.LightGray else Color.Transparent,
                                    cornerRadius = CornerRadius(15)
                                )
                            }
                            .padding(
                                horizontal = if (date.dayOfMonth == 1) 0.dp else 12.dp,
                                vertical = if (date.dayOfMonth == 1) 0.dp else 8.dp
                            ),
                        text = if (date.dayOfMonth == 1) {
                            "${date.monthValue} / ${date.dayOfMonth}"
                        } else {
                            date.dayOfMonth.toString()
                        },
                        color = if (date >= state.minDate && date <= LocalDate.now()) Color.Black else Color.LightGray
                    )
                }
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
                val item = dailyPagingItems[it]?.get(SortType.UsageEvent)
                if (item != null) {
                    DailyUsageChart(
                        title = item.sumOf { it.second }.convertToRealUsageTime(),
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
                        title = "포그라운드 실행 시간 " +
                                item.sumOf { it.second }
                                    .convertToRealUsageTime(),
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
}