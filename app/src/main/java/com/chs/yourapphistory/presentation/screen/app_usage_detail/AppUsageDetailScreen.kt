package com.chs.yourapphistory.presentation.screen.app_usage_detail

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
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertToRealUsageHour
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.divideDayOfWeek
import com.chs.yourapphistory.common.getYearOfWeek
import com.chs.yourapphistory.common.toConvertDisplayYearDate
import com.chs.yourapphistory.common.toDisplayYearDate
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
    AppUsageDetailScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                AppUsageDetailEvent.OnBackClick -> {
                    onBack()
                }

                else -> Unit
            }
            viewModel.changeEvent(event)
        }
    )
}

@Composable
fun AppUsageDetailScreen(
    state: AppUsageDetailState,
    onEvent: (AppUsageDetailEvent) -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val appUsedPagingData = state.pagingDailyUsedInfo?.collectAsLazyPagingItems()
    val appUsedPagerState = if (appUsedPagingData != null && appUsedPagingData.itemCount != 0) {
        rememberPagerState(
            pageCount = {
                appUsedPagingData.itemCount
            }, initialPage = appUsedPagingData.itemSnapshotList.items.map {
                it.first
            }.indexOf(state.displayDate)
        )
    } else {
        rememberPagerState(pageCount = { 0 })
    }
    val appForegroundUsedPagingData =
        state.pagingDailyForegroundUsedInfo?.collectAsLazyPagingItems()
    val appForegroundPagerState =
        if (appForegroundUsedPagingData != null && appForegroundUsedPagingData.itemCount != 0) {
            rememberPagerState(
                pageCount = {
                    appForegroundUsedPagingData.itemCount
                }, initialPage = appForegroundUsedPagingData.itemSnapshotList.items.map {
                    it.first
                }.indexOf(state.displayDate)
            )
        } else {
            rememberPagerState(pageCount = { 0 })
        }
    val appNotifyPagingData = state.pagingDailyNotifyInfo?.collectAsLazyPagingItems()
    val appNotifyPagerState =
        if (appNotifyPagingData != null && appNotifyPagingData.itemCount != 0) {
            rememberPagerState(
                pageCount = {
                    appNotifyPagingData.itemCount
                }, initialPage = appNotifyPagingData.itemSnapshotList.items.map {
                    it.first
                }.indexOf(state.displayDate)
            )
        } else {
            rememberPagerState(pageCount = { 0 })
        }
    val appLaunchPagingData = state.pagingDailyLaunchInfo?.collectAsLazyPagingItems()
    val appLaunchPagerState =
        if (appLaunchPagingData != null && appLaunchPagingData.itemCount != 0) {
            rememberPagerState(
                pageCount = {
                    appLaunchPagingData.itemCount
                }, initialPage = appLaunchPagingData.itemSnapshotList.items.map {
                    it.first
                }.indexOf(state.displayDate)
            )
        } else {
            rememberPagerState(pageCount = { 0 })
        }

    val datePagerState = if (state.dateList.isNotEmpty()) {
        rememberPagerState(
            pageCount = { state.dateList.count() },
            initialPage = state.dateList.flatten().indexOf(state.displayDate) / 7
        )
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    val appUsedWeekPagingData = state.pagingWeeklyUsedInfo?.collectAsLazyPagingItems()
    val appUsedWeekPagerState =
        if (appUsedWeekPagingData != null && appUsedWeekPagingData.itemCount != 0) {
            rememberPagerState(
                pageCount = {
                    appUsedWeekPagingData.itemCount
                }, initialPage = appUsedWeekPagingData.itemSnapshotList.items.map {
                    it.first
                }.indexOf(state.displayWeek)
            )
        } else {
            rememberPagerState(pageCount = { 0 })
        }

    val appForegroundWeekPagingData = state.pagingWeeklyForegroundInfo?.collectAsLazyPagingItems()
    val appForegroundWeekPagerState =
        if (appForegroundWeekPagingData != null && appForegroundWeekPagingData.itemCount != 0) {
            rememberPagerState(
                pageCount = {
                    appForegroundWeekPagingData.itemCount
                }, initialPage = appForegroundWeekPagingData.itemSnapshotList.items.map {
                    it.first
                }.indexOf(state.displayWeek)
            )
        } else {
            rememberPagerState(pageCount = { 0 })
        }

    val appNotifyWeekPagingData = state.pagingWeeklyNotifyInfo?.collectAsLazyPagingItems()
    val appNotifyWeekPagerState =
        if (appNotifyWeekPagingData != null && appNotifyWeekPagingData.itemCount != 0) {
            rememberPagerState(
                pageCount = {
                    appNotifyWeekPagingData.itemCount
                }, initialPage = appNotifyWeekPagingData.itemSnapshotList.items.map {
                    it.first
                }.indexOf(state.displayWeek)
            )
        } else {
            rememberPagerState(pageCount = { 0 })
        }

    val appLaunchWeekPagingData = state.pagingWeeklyLaunchInfo?.collectAsLazyPagingItems()
    val appLaunchWeekPagerState =
        if (appLaunchWeekPagingData != null && appLaunchWeekPagingData.itemCount != 0) {
            rememberPagerState(
                pageCount = {
                    appLaunchWeekPagingData.itemCount
                }, initialPage = appLaunchWeekPagingData.itemSnapshotList.items.map {
                    it.first
                }.indexOf(state.displayWeek)
            )
        } else {
            rememberPagerState(pageCount = { 0 })
        }

    val weekPagerState = if (state.weekList.isNotEmpty()) {
        rememberPagerState(
            pageCount = { state.weekList.count() },
            initialPage = state.weekList.indexOf(state.displayWeek) / 5
        )
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    var selectDateIdx by remember { mutableIntStateOf(0) }
    var selectWeekIdx by remember { mutableIntStateOf(0) }

    LaunchedEffect(appUsedPagerState.isScrollInProgress) {

        if (appUsedPagingData == null) return@LaunchedEffect

        if (appUsedPagingData.itemCount == 0) return@LaunchedEffect

        if (appUsedPagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetDate(
                appUsedPagingData[appUsedPagerState.currentPage]?.first ?: LocalDate.now()
            )
        )
    }

    LaunchedEffect(appForegroundPagerState.isScrollInProgress) {

        if (appForegroundUsedPagingData == null) return@LaunchedEffect

        if (appForegroundUsedPagingData.itemCount == 0) return@LaunchedEffect

        if (appForegroundPagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetDate(
                appForegroundUsedPagingData[appForegroundPagerState.currentPage]?.first
                    ?: LocalDate.now()
            )
        )
    }

    LaunchedEffect(appNotifyPagerState.isScrollInProgress) {

        if (appNotifyPagingData == null) return@LaunchedEffect

        if (appNotifyPagingData.itemCount == 0) return@LaunchedEffect

        if (appNotifyPagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetDate(
                appNotifyPagingData[appNotifyPagerState.currentPage]?.first ?: LocalDate.now()
            )
        )
    }

    LaunchedEffect(appLaunchPagerState.isScrollInProgress) {

        if (appLaunchPagingData == null) return@LaunchedEffect

        if (appLaunchPagingData.itemCount == 0) return@LaunchedEffect

        if (appLaunchPagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetDate(
                appLaunchPagingData[appLaunchPagerState.currentPage]?.first ?: LocalDate.now()
            )
        )
    }

    LaunchedEffect(state.displayDate) {
        if (state.dateList.isEmpty()) return@LaunchedEffect

        if (state.dateList[datePagerState.currentPage].min() > state.displayDate) {
            launch {
                datePagerState.scrollToPage(datePagerState.currentPage + 1)
                selectDateIdx = 6
            }
        }

        if (state.dateList[datePagerState.currentPage].max() < state.displayDate) {
            launch {
                datePagerState.scrollToPage(datePagerState.currentPage - 1)
                selectDateIdx = 0
            }
        }

        awaitAll(
            async {
                if (appUsedPagingData == null || appUsedPagingData.itemCount == 0) return@async
                if (appUsedPagingData[appUsedPagerState.currentPage]!!.first == state.displayDate)
                    return@async
                appUsedPagerState.scrollToPage(
                    appUsedPagingData.itemSnapshotList.map { it!!.first }.indexOf(state.displayDate)
                )
            },
            async {

                if (appForegroundUsedPagingData == null || appForegroundUsedPagingData.itemCount == 0) return@async
                if (appForegroundUsedPagingData[appForegroundPagerState.currentPage]!!.first == state.displayDate)
                    return@async
                appForegroundPagerState.scrollToPage(
                    appForegroundUsedPagingData.itemSnapshotList.map { it!!.first }
                        .indexOf(state.displayDate)
                )
            },
            async {

                if (appNotifyPagingData == null || appNotifyPagingData.itemCount == 0) return@async
                if (appNotifyPagingData[appNotifyPagerState.currentPage]!!.first == state.displayDate)
                    return@async
                appNotifyPagerState.scrollToPage(
                    appNotifyPagingData.itemSnapshotList.map { it!!.first }
                        .indexOf(state.displayDate)
                )
            },
            async {

                if (appLaunchPagingData == null || appLaunchPagingData.itemCount == 0) return@async
                if (appLaunchPagingData[appLaunchPagerState.currentPage]!!.first == state.displayDate)
                    return@async
                appLaunchPagerState.scrollToPage(
                    appLaunchPagingData.itemSnapshotList.map { it!!.first }
                        .indexOf(state.displayDate)
                )
            }
        )
    }

    LaunchedEffect(datePagerState.isScrollInProgress) {

        if (state.dateList.isEmpty()) return@LaunchedEffect

        if (state.displayDate == state.dateList[datePagerState.currentPage][6 - selectDateIdx])
            return@LaunchedEffect

        if (datePagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect

        if (state.dateList[datePagerState.currentPage][6 - selectDateIdx] > LocalDate.now()) {
            onEvent(
                AppUsageDetailEvent.OnChangeTargetDate(LocalDate.now())
            )
            return@LaunchedEffect
        }


        if (state.dateList[datePagerState.currentPage][6 - selectDateIdx] < state.minDate) {
            onEvent(
                AppUsageDetailEvent.OnChangeTargetDate(state.minDate)
            )
            return@LaunchedEffect
        }

        onEvent(
            AppUsageDetailEvent.OnChangeTargetDate(
                state.dateList[datePagerState.currentPage][6 - selectDateIdx]
            )
        )
    }

    LaunchedEffect(appUsedWeekPagerState.isScrollInProgress) {

        if (appUsedWeekPagingData == null) return@LaunchedEffect

        if (appUsedWeekPagingData.itemCount == 0) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetWeek(
                appUsedWeekPagingData[appUsedWeekPagerState.currentPage]?.first?.max()
                    ?: LocalDate.now()
            )
        )
    }

    LaunchedEffect(appForegroundWeekPagerState.isScrollInProgress) {

        if (appForegroundWeekPagingData == null) return@LaunchedEffect

        if (appForegroundWeekPagingData.itemCount == 0) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetWeek(
                appForegroundWeekPagingData[appForegroundWeekPagerState.currentPage]?.first?.max()
                    ?: LocalDate.now()
            )
        )
    }

    LaunchedEffect(appNotifyWeekPagerState.isScrollInProgress) {

        if (appNotifyWeekPagingData == null) return@LaunchedEffect

        if (appNotifyWeekPagingData.itemCount == 0) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetWeek(
                appNotifyWeekPagingData[appNotifyWeekPagerState.currentPage]?.first?.max()
                    ?: LocalDate.now()
            )
        )
    }

    LaunchedEffect(appLaunchWeekPagerState.isScrollInProgress) {

        if (appLaunchWeekPagingData == null) return@LaunchedEffect

        if (appLaunchWeekPagingData.itemCount == 0) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetWeek(
                appLaunchWeekPagingData[appLaunchWeekPagerState.currentPage]?.first?.max()
                    ?: LocalDate.now()
            )
        )
    }

    LaunchedEffect(state.displayWeek) {
        if (state.weekList.isEmpty()) return@LaunchedEffect

        if (state.weekList[weekPagerState.currentPage].min() > state.displayWeek.max()) {
            launch {
                weekPagerState.scrollToPage(weekPagerState.currentPage + 1)
                selectWeekIdx = state.weekList[weekPagerState.currentPage + 1].count() - 1
            }
        }

        if (state.weekList[weekPagerState.currentPage].max() < state.displayWeek.max()) {
            launch {
                weekPagerState.scrollToPage(weekPagerState.currentPage - 1)
                selectWeekIdx = 0
            }
        }

        awaitAll(
            async {
                if (appUsedWeekPagingData == null || appUsedWeekPagingData.itemCount == 0) return@async
                if (appUsedWeekPagingData[appUsedWeekPagerState.currentPage]!!.first == state.displayWeek)
                    return@async
                appUsedWeekPagerState.scrollToPage(
                    appUsedWeekPagingData.itemSnapshotList.map { it!!.first }
                        .indexOf(state.displayWeek)
                )
            },
            async {
                if (appForegroundWeekPagingData == null || appForegroundWeekPagingData.itemCount == 0) return@async
                if (appForegroundWeekPagingData[appForegroundWeekPagerState.currentPage]!!.first == state.displayWeek)
                    return@async
                appForegroundWeekPagerState.scrollToPage(
                    appForegroundWeekPagingData.itemSnapshotList.map { it!!.first }
                        .indexOf(state.displayWeek)
                )
            },
            async {
                if (appNotifyWeekPagingData == null || appNotifyWeekPagingData.itemCount == 0) return@async
                if (appNotifyWeekPagingData[appNotifyWeekPagerState.currentPage]!!.first == state.displayWeek)
                    return@async
                appNotifyWeekPagerState.scrollToPage(
                    appNotifyWeekPagingData.itemSnapshotList.map { it!!.first }
                        .indexOf(state.displayWeek)
                )
            },
            async {
                if (appLaunchWeekPagingData == null || appLaunchWeekPagingData.itemCount == 0) return@async
                if (appLaunchWeekPagingData[appLaunchWeekPagerState.currentPage]!!.first == state.displayWeek)
                    return@async
                appLaunchWeekPagerState.scrollToPage(
                    appLaunchWeekPagingData.itemSnapshotList.map { it!!.first }
                        .indexOf(state.displayWeek)
                )
            }
        )
    }

    LaunchedEffect(weekPagerState.isScrollInProgress) {
        if (state.weekList.isEmpty()) return@LaunchedEffect
        val idx = state.weekList[weekPagerState.currentPage].count() - 1 - selectWeekIdx

        if (state.displayWeek.max() == state.dateList[datePagerState.currentPage][idx])
            return@LaunchedEffect

        if (weekPagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetWeek(
                state.weekList[weekPagerState.currentPage][idx]
            )
        )
    }

    BackHandler { onEvent(AppUsageDetailEvent.OnBackClick) }

    LaunchedEffect(datePagerState.currentPage) {
        chsLog(datePagerState.currentPage.toString())
    }

    ItemPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(500L)
                isRefreshing = false
            }
        }
    ) {
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
                Column {
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
                }

                Text(
                    modifier = Modifier
                        .clickable { onEvent(AppUsageDetailEvent.OnChangeViewType) },
                    text = if (state.isDailyMode) {
                        "주별 보기"
                    } else {
                        "일별 보기"
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }

            if (state.isDailyMode) {
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    state = datePagerState,
                    reverseLayout = true,
                    userScrollEnabled = true,
                    key = { state.dateList[it] }
                ) {
                    val item = state.dateList[it]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        item.reversed().forEachIndexed { idx, date ->
                            if (state.displayDate == date) {
                                selectDateIdx = idx
                            }
                            if (date.dayOfMonth == 1) {
                                Text(
                                    modifier = Modifier
                                        .clickable {
                                            if (date >= state.minDate && date <= LocalDate.now()) {
                                                onEvent(AppUsageDetailEvent.OnChangeTargetDate(date))
                                            }
                                        }
                                        .drawBehind {
                                            drawRoundRect(
                                                color = if (state.displayDate == date) Color.LightGray else Color.Transparent,
                                                cornerRadius = CornerRadius(
                                                    15.dp.toPx(),
                                                    15.dp.toPx()
                                                )
                                            )
                                        }
                                        .padding(8.dp),
                                    text = "${date.monthValue} / ${date.dayOfMonth}",
                                    color = if (date >= state.minDate && date <= LocalDate.now()) Color.Black else Color.LightGray
                                )
                            } else {
                                Text(
                                    modifier = Modifier
                                        .clickable {
                                            if (date >= state.minDate && date <= LocalDate.now()) {
                                                onEvent(AppUsageDetailEvent.OnChangeTargetDate(date))
                                            }
                                        }
                                        .drawBehind {
                                            drawRoundRect(
                                                color = if (state.displayDate == date) Color.LightGray else Color.Transparent,
                                                cornerRadius = CornerRadius(
                                                    15.dp.toPx(),
                                                    15.dp.toPx()
                                                )
                                            )
                                        }
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        ),
                                    text = date.dayOfMonth.toString(),
                                    color = if (date >= state.minDate && date <= LocalDate.now()) Color.Black else Color.LightGray
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (appUsedPagingData != null && appUsedPagingData.itemCount != 0) {
                        HorizontalPager(
                            modifier = Modifier
                                .fillMaxSize(),
                            pageSpacing = 8.dp,
                            state = appUsedPagerState,
                            reverseLayout = true,
                            userScrollEnabled = true,
                            key = appUsedPagingData.itemKey { it.first }
                        ) { page ->
                            val item = appUsedPagingData[page]?.second
                            if (item != null) {
                                DailyUsageChart(
                                    title = item.sumOf { it.second }.convertToRealUsageTime(),
                                    list = item,
                                    convertText = { it.convertToRealUsageMinutes() }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (appForegroundUsedPagingData != null && appForegroundUsedPagingData.itemCount != 0) {
                        HorizontalPager(
                            modifier = Modifier
                                .fillMaxSize(),
                            pageSpacing = 8.dp,
                            state = appForegroundPagerState,
                            reverseLayout = true,
                            userScrollEnabled = true,
                            key = appForegroundUsedPagingData.itemKey { it.first }
                        ) { page ->
                            val item = appForegroundUsedPagingData[page]?.second
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
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (appNotifyPagingData != null && appNotifyPagingData.itemCount != 0) {
                        HorizontalPager(
                            modifier = Modifier
                                .fillMaxSize(),
                            pageSpacing = 8.dp,
                            state = appNotifyPagerState,
                            reverseLayout = true,
                            userScrollEnabled = true,
                            key = appNotifyPagingData.itemKey { it.first }
                        ) { page ->
                            val item = appNotifyPagingData[page]?.second
                            if (item != null) {
                                DailyUsageChart(
                                    title = "알림 ${item.sumOf { it.second }}개",
                                    list = item,
                                    convertText = { "${it}개" }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (appLaunchPagingData != null && appLaunchPagingData.itemCount != 0) {
                        HorizontalPager(
                            modifier = Modifier
                                .fillMaxSize(),
                            pageSpacing = 8.dp,
                            state = appLaunchPagerState,
                            reverseLayout = true,
                            userScrollEnabled = true,
                            key = appLaunchPagingData.itemKey { it.first }
                        ) { page ->
                            val item = appLaunchPagingData[page]?.second
                            if (item != null) {
                                DailyUsageChart(
                                    title = "총 실행 횟수 ${item.sumOf { it.second }}회",
                                    list = item,
                                    convertText = { "${it}회" }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    state = weekPagerState,
                    reverseLayout = true,
                    userScrollEnabled = true,
                    key = { state.weekList[it] }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val item = state.weekList[it]
                        item.reversed().forEachIndexed { idx, maxDate ->
                            if (state.displayWeek.any { it == maxDate }) {
                                selectWeekIdx = idx
                            }
                            Text(
                                modifier = Modifier
                                    .clickable {
                                        onEvent(AppUsageDetailEvent.OnChangeTargetWeek(maxDate))
                                    }
                                    .drawBehind {
                                        drawRoundRect(
                                            color = if (state.displayWeek.any { it == maxDate }) {
                                                Color.LightGray
                                            } else Color.Transparent,
                                            cornerRadius = CornerRadius(15.dp.toPx(), 15.dp.toPx())
                                        )
                                    }
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    ),
                                text = "${maxDate.getYearOfWeek()}주"
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (appUsedWeekPagingData != null && appUsedWeekPagingData.itemCount != 0) {
                        HorizontalPager(
                            modifier = Modifier
                                .fillMaxSize(),
                            pageSpacing = 8.dp,
                            state = appUsedWeekPagerState,
                            reverseLayout = true,
                            userScrollEnabled = true,
                            key = appUsedWeekPagingData.itemKey { it.first }
                        ) { page ->
                            val item = appUsedWeekPagingData[page]?.second
                            if (item != null) {
                                WeeklyUsageChart(
                                    title = "${
                                        item.sumOf { it.second }
                                            .divideDayOfWeek().convertToRealUsageHour()
                                    }/일",
                                    subTitle = buildAnnotatedString {
                                        append("이번 주 총 ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append(item.sumOf { it.second }
                                                .convertToRealUsageHour())
                                        }
                                    },
                                    list = item,
                                    convertText = { it.convertToRealUsageHour() }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (appForegroundWeekPagingData != null && appForegroundWeekPagingData.itemCount != 0) {
                        HorizontalPager(
                            modifier = Modifier
                                .fillMaxSize(),
                            pageSpacing = 8.dp,
                            state = appForegroundWeekPagerState,
                            reverseLayout = true,
                            userScrollEnabled = true,
                            key = appForegroundWeekPagingData.itemKey { it.first }
                        ) { page ->
                            val item = appForegroundWeekPagingData[page]?.second
                            if (item != null) {
                                WeeklyUsageChart(
                                    title = "${
                                        item.sumOf { it.second }
                                            .divideDayOfWeek().convertToRealUsageHour()
                                    }/일",
                                    subTitle = buildAnnotatedString {
                                        append("이번 주 총 ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append(item.sumOf { it.second }
                                                .convertToRealUsageHour())
                                        }
                                    },
                                    list = item,
                                    convertText = { it.convertToRealUsageHour() }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (appNotifyWeekPagingData != null && appNotifyWeekPagingData.itemCount != 0) {
                        HorizontalPager(
                            modifier = Modifier
                                .fillMaxSize(),
                            pageSpacing = 8.dp,
                            state = appNotifyWeekPagerState,
                            reverseLayout = true,
                            userScrollEnabled = true,
                            key = appNotifyWeekPagingData.itemKey { it.first }
                        ) { page ->
                            val item = appNotifyWeekPagingData[page]?.second
                            if (item != null) {
                                WeeklyUsageChart(
                                    title = "알림 ${
                                        item.sumOf { it.second }
                                            .divideDayOfWeek()
                                    }개/일",
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
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (appLaunchWeekPagingData != null && appLaunchWeekPagingData.itemCount != 0) {
                        HorizontalPager(
                            modifier = Modifier
                                .fillMaxSize(),
                            pageSpacing = 8.dp,
                            state = appLaunchWeekPagerState,
                            reverseLayout = true,
                            userScrollEnabled = true,
                            key = appLaunchWeekPagingData.itemKey { it.first }
                        ) { page ->
                            val item = appLaunchWeekPagingData[page]?.second
                            if (item != null) {
                                WeeklyUsageChart(
                                    title = "앱 실행 ${
                                        item.sumOf { it.second }
                                            .divideDayOfWeek()
                                    }회/일",
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
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}