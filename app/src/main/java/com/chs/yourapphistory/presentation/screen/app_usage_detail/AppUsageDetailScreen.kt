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
    val dailyPagerState = rememberPagerState(pageCount = { dailyPagingItems.itemCount })
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
            if (state.isDailyMode) {
                Text(
                    text = if (state.displayDate == LocalDate.now()) {
                        "오늘"
                    } else {
                        state.displayDate.toConvertDisplayYearDate()
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            } else {
                Text(
                    text = state.displayWeek.toDisplayYearDate(),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }

            Text(
                modifier = Modifier
                    .clickable { onIntent(AppUsageDetailIntent.OnChangeViewType) },
                text = if (state.isDailyMode) {
                    "주별 보기"
                } else {
                    "일별 보기"
                },
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }


        repeat(4) {
            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                state = dailyPagerState,
                reverseLayout = true,
                userScrollEnabled = true,
                key = { it }
            ) {
               val item = dailyPagingItems[it]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(state = scrollState),
                ) {
                    item?.forEach { info ->
                        when (info.key) {
                            SortType.UsageEvent -> {
                                DailyUsageChart(
                                    title = info.value.sumOf { it.second }.convertToRealUsageTime(),
                                    list = info.value,
                                    convertText = { it.convertToRealUsageMinutes() }
                                )

                                Spacer(modifier = Modifier.height(32.dp))
                            }
                            SortType.ForegroundUsageEvent -> {
                                DailyUsageChart(
                                    title = "포그라운드 실행 시간 " +
                                            info.value.sumOf { it.second }
                                                .convertToRealUsageTime(),
                                    list = info.value,
                                    convertText = { it.convertToRealUsageMinutes() }
                                )

                                Spacer(modifier = Modifier.height(32.dp))
                            }
                            SortType.NotifyEvent -> {
                                DailyUsageChart(
                                    title = "알림 ${info.value.sumOf { it.second }}개",
                                    list = info.value,
                                    convertText = { "${it}개" }
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                            SortType.LaunchEvent -> {
                                DailyUsageChart(
                                    title = "총 실행 횟수 ${info.value.sumOf { it.second }}회",
                                    list = info.value,
                                    convertText = { "${it}회" }
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }
//dp
//        if (state.isDailyMode) {
//            HorizontalPager(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 8.dp),
//                state = datePagerState,
//                reverseLayout = true,
//                userScrollEnabled = true,
//                key = { state.dateList[it] }
//            ) {
//                val item = state.dateList[it]
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceAround
//                ) {
//                    item.reversed().forEachIndexed { idx, date ->
//                        if (state.displayDate == date) {
//                            selectDateIdx = idx
//                        }
//                        if (date.dayOfMonth == 1) {
//                            Text(
//                                modifier = Modifier
//                                    .clickable {
//                                        if (date >= state.minDate && date <= LocalDate.now()) {
//                                            onIntent(AppUsageDetailIntent.OnChangeTargetDate(date))
//                                        }
//                                    }
//                                    .drawBehind {
//                                        drawRoundRect(
//                                            color = if (state.displayDate == date) Color.LightGray else Color.Transparent,
//                                            cornerRadius = CornerRadius(
//                                                15.dp.toPx(),
//                                                15.dp.toPx()
//                                            )
//                                        )
//                                    }
//                                    .padding(8.dp),
//                                text = "${date.monthValue} / ${date.dayOfMonth}",
//                                color = if (date >= state.minDate && date <= LocalDate.now()) Color.Black else Color.LightGray
//                            )
//                        } else {
//                            Text(
//                                modifier = Modifier
//                                    .clickable {
//                                        if (date >= state.minDate && date <= LocalDate.now()) {
//                                            onIntent(AppUsageDetailIntent.OnChangeTargetDate(date))
//                                        }
//                                    }
//                                    .drawBehind {
//                                        drawRoundRect(
//                                            color = if (state.displayDate == date) Color.LightGray else Color.Transparent,
//                                            cornerRadius = CornerRadius(
//                                                15.dp.toPx(),
//                                                15.dp.toPx()
//                                            )
//                                        )
//                                    }
//                                    .padding(
//                                        horizontal = 12.dp,
//                                        vertical = 8.dp
//                                    ),
//                                text = date.dayOfMonth.toString(),
//                                color = if (date >= state.minDate && date <= LocalDate.now()) Color.Black else Color.LightGray
//                            )
//                        }
//                    }
//                }
//            }
//
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .verticalScroll(rememberScrollState())
//            ) {
//                if (appUsedPagingData != null && appUsedPagingData.itemCount != 0) {
//                    HorizontalPager(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        pageSpacing = 8.dp,
//                        state = appUsedPagerState,
//                        reverseLayout = true,
//                        userScrollEnabled = true,
//                        key = appUsedPagingData.itemKey { it.first }
//                    ) { page ->
//                        val item = appUsedPagingData[page]?.second
//                        if (item != null) {
//                            DailyUsageChart(
//                                title = item.sumOf { it.second }.convertToRealUsageTime(),
//                                list = item,
//                                convertText = { it.convertToRealUsageMinutes() }
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                if (appForegroundUsedPagingData != null && appForegroundUsedPagingData.itemCount != 0) {
//                    HorizontalPager(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        pageSpacing = 8.dp,
//                        state = appForegroundPagerState,
//                        reverseLayout = true,
//                        userScrollEnabled = true,
//                        key = appForegroundUsedPagingData.itemKey { it.first }
//                    ) { page ->
//                        val item = appForegroundUsedPagingData[page]?.second
//                        if (item != null) {
//                            DailyUsageChart(
//                                title = "포그라운드 실행 시간 " +
//                                        item.sumOf { it.second }
//                                            .convertToRealUsageTime(),
//                                list = item,
//                                convertText = { it.convertToRealUsageMinutes() }
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                if (appNotifyPagingData != null && appNotifyPagingData.itemCount != 0) {
//                    HorizontalPager(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        pageSpacing = 8.dp,
//                        state = appNotifyPagerState,
//                        reverseLayout = true,
//                        userScrollEnabled = true,
//                        key = appNotifyPagingData.itemKey { it.first }
//                    ) { page ->
//                        val item = appNotifyPagingData[page]?.second
//                        if (item != null) {
//                            DailyUsageChart(
//                                title = "알림 ${item.sumOf { it.second }}개",
//                                list = item,
//                                convertText = { "${it}개" }
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                if (appLaunchPagingData != null && appLaunchPagingData.itemCount != 0) {
//                    HorizontalPager(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        pageSpacing = 8.dp,
//                        state = appLaunchPagerState,
//                        reverseLayout = true,
//                        userScrollEnabled = true,
//                        key = appLaunchPagingData.itemKey { it.first }
//                    ) { page ->
//                        val item = appLaunchPagingData[page]?.second
//                        if (item != null) {
//                            DailyUsageChart(
//                                title = "총 실행 횟수 ${item.sumOf { it.second }}회",
//                                list = item,
//                                convertText = { "${it}회" }
//                            )
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.height(32.dp))
//            }
//        } else {
//            HorizontalPager(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 8.dp),
//                state = weekPagerState,
//                reverseLayout = true,
//                userScrollEnabled = true,
//                key = { state.weekList[it] }
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceAround
//                ) {
//                    val item = state.weekList[it]
//                    item.reversed().forEachIndexed { idx, maxDate ->
//                        if (state.displayWeek.any { it == maxDate }) {
//                            selectWeekIdx = idx
//                        }
//                        Text(
//                            modifier = Modifier
//                                .clickable {
//                                    onIntent(AppUsageDetailIntent.OnChangeTargetWeek(maxDate))
//                                }
//                                .drawBehind {
//                                    drawRoundRect(
//                                        color = if (state.displayWeek.any { it == maxDate }) {
//                                            Color.LightGray
//                                        } else Color.Transparent,
//                                        cornerRadius = CornerRadius(15.dp.toPx(), 15.dp.toPx())
//                                    )
//                                }
//                                .padding(
//                                    horizontal = 12.dp,
//                                    vertical = 8.dp
//                                ),
//                            text = "${maxDate.getYearOfWeek()}주"
//                        )
//                    }
//                }
//            }
//
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .verticalScroll(rememberScrollState())
//            ) {
//                if (appUsedWeekPagingData != null && appUsedWeekPagingData.itemCount != 0) {
//                    HorizontalPager(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        pageSpacing = 8.dp,
//                        state = appUsedWeekPagerState,
//                        reverseLayout = true,
//                        userScrollEnabled = true,
//                        key = appUsedWeekPagingData.itemKey { it.first }
//                    ) { page ->
//                        val item = appUsedWeekPagingData[page]?.second
//                        if (!item.isNullOrEmpty()) {
//                            WeeklyUsageChart(
//                                title = "${
//                                    item.toCalcDailyUsage()
//                                }/일",
//                                subTitle = buildAnnotatedString {
//                                    append("이번 주 총 ")
//                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                                        append(item.sumOf { it.second }
//                                            .convertToRealUsageHour())
//                                    }
//                                },
//                                list = item,
//                                convertText = { it.convertToRealUsageHour() }
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                if (appForegroundWeekPagingData != null && appForegroundWeekPagingData.itemCount != 0) {
//                    HorizontalPager(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        pageSpacing = 8.dp,
//                        state = appForegroundWeekPagerState,
//                        reverseLayout = true,
//                        userScrollEnabled = true,
//                        key = appForegroundWeekPagingData.itemKey { it.first }
//                    ) { page ->
//                        val item = appForegroundWeekPagingData[page]?.second
//                        if (!item.isNullOrEmpty()) {
//                            WeeklyUsageChart(
//                                title = "${
//                                    item.toCalcDailyUsage()
//                                }/일",
//                                subTitle = buildAnnotatedString {
//                                    append("이번 주 총 ")
//                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                                        append(item.sumOf { it.second }
//                                            .convertToRealUsageHour())
//                                    }
//                                },
//                                list = item,
//                                convertText = { it.convertToRealUsageHour() }
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                if (appNotifyWeekPagingData != null && appNotifyWeekPagingData.itemCount != 0) {
//                    HorizontalPager(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        pageSpacing = 8.dp,
//                        state = appNotifyWeekPagerState,
//                        reverseLayout = true,
//                        userScrollEnabled = true,
//                        key = appNotifyWeekPagingData.itemKey { it.first }
//                    ) { page ->
//                        val item = appNotifyWeekPagingData[page]?.second
//                        if (!item.isNullOrEmpty()) {
//                            WeeklyUsageChart(
//                                title = "알림 ${
//                                    item.toCalcDailyCount()
//                                }개/일",
//                                subTitle = buildAnnotatedString {
//                                    append("이번 주 총 알림 ")
//                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                                        append(item.sumOf { it.second }.toString())
//                                    }
//                                    append("개")
//                                },
//                                list = item,
//                                convertText = { "${it}개" }
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                if (appLaunchWeekPagingData != null && appLaunchWeekPagingData.itemCount != 0) {
//                    HorizontalPager(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        pageSpacing = 8.dp,
//                        state = appLaunchWeekPagerState,
//                        reverseLayout = true,
//                        userScrollEnabled = true,
//                        key = appLaunchWeekPagingData.itemKey { it.first }
//                    ) { page ->
//                        val item = appLaunchWeekPagingData[page]?.second
//                        if (!item.isNullOrEmpty()) {
//                            WeeklyUsageChart(
//                                title = "앱 실행 ${
//                                    item.toCalcDailyCount()
//                                }회/일",
//                                subTitle = buildAnnotatedString {
//                                    append("이번 주 총 앱 실행 ")
//                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                                        append(item.sumOf { it.second }.toString())
//                                    }
//                                    append("회")
//                                },
//                                list = item,
//                                convertText = { "${it}번" }
//                            )
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.height(32.dp))
//            }
//        }
    }
}