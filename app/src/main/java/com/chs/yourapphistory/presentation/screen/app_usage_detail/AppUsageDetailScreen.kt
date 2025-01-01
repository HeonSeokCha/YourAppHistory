package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Card
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.presentation.screen.common.ItemPullToRefreshBox
import com.chs.yourapphistory.presentation.screen.common.PlaceholderHighlight
import com.chs.yourapphistory.presentation.screen.common.UsageChart
import com.chs.yourapphistory.presentation.screen.common.placeholder
import com.chs.yourapphistory.presentation.screen.common.shimmer
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


    val appUsedPagingData = state.pagingUsedInfo?.collectAsLazyPagingItems()
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
    val appForegroundUsedPagingData = state.pagingForegroundUsedInfo?.collectAsLazyPagingItems()
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
    val appNotifyPagingData = state.pagingNotifyInfo?.collectAsLazyPagingItems()
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
    val appLaunchPagingData = state.pagingLaunchInfo?.collectAsLazyPagingItems()
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

    LaunchedEffect(appUsedPagerState.currentPage) {
        if (appUsedPagingData == null) return@LaunchedEffect

        if (appUsedPagingData.itemCount == 0) return@LaunchedEffect

        onEvent(
            AppUsageDetailEvent.OnChangeTargetDate(
                appUsedPagingData[appUsedPagerState.currentPage]?.first ?: LocalDate.now()
            )
        )

        launch {
            val a = async {
                delay(200L)
                appForegroundPagerState.animateScrollToPage(page = appUsedPagerState.currentPage)
            }

            val b = async {
                delay(200L)
                appNotifyPagerState.animateScrollToPage(page = appUsedPagerState.currentPage)
            }

            val c = async {
                delay(200L)
                appLaunchPagerState.animateScrollToPage(page = appUsedPagerState.currentPage)
            }
            awaitAll(a, b, c)
        }
    }

//    LaunchedEffect(appForegroundPagerState.currentPage) {
//        if (appForegroundUsedPagingData == null) return@LaunchedEffect
//
//        if (appForegroundUsedPagingData.itemCount == 0) return@LaunchedEffect
//
//        onEvent(
//            AppUsageDetailEvent.OnChangeTargetDate(
//                appForegroundUsedPagingData[appForegroundPagerState.currentPage]?.first
//                    ?: LocalDate.now()
//            )
//        )
//    }

    LaunchedEffect(appNotifyPagerState.currentPage) {
        if (appNotifyPagingData == null) return@LaunchedEffect

        if (appNotifyPagingData.itemCount == 0) return@LaunchedEffect
        onEvent(
            AppUsageDetailEvent.OnChangeTargetDate(
                appNotifyPagingData[appNotifyPagerState.currentPage]?.first ?: LocalDate.now()
            )
        )

        launch {
            val a = async {
                delay(200L)
                appForegroundPagerState.animateScrollToPage(page = appUsedPagerState.currentPage)
            }

            val b = async {
                delay(200L)
                appUsedPagerState.animateScrollToPage(page = appUsedPagerState.currentPage)
            }

            val c = async {
                delay(200L)
                appLaunchPagerState.animateScrollToPage(page = appUsedPagerState.currentPage)
            }
            awaitAll(a, b, c)
        }
    }

    LaunchedEffect(appLaunchPagerState.currentPage) {
        if (appLaunchPagingData == null) return@LaunchedEffect
        if (appLaunchPagingData.itemCount == 0) return@LaunchedEffect
        onEvent(
            AppUsageDetailEvent.OnChangeTargetDate(
                appLaunchPagingData[appLaunchPagerState.currentPage]?.first ?: LocalDate.now()
            )
        )

        launch {
            val a = async {
                delay(200L)
                appForegroundPagerState.animateScrollToPage(page = appUsedPagerState.currentPage)
            }

            val b = async {
                delay(200L)
                appNotifyPagerState.animateScrollToPage(page = appUsedPagerState.currentPage)
            }

            val c = async {
                delay(200L)
                appUsedPagerState.animateScrollToPage(page = appUsedPagerState.currentPage)
            }
            awaitAll(a, b, c)
        }
    }

    BackHandler { onEvent(AppUsageDetailEvent.OnBackClick) }

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
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = if (state.displayDate == LocalDate.now()) {
                        "오늘"
                    } else {
                        state.displayDate.format(Constants.DATE_FORMAT)
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
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
                            UsageChart(
                                title = item.sumOf { it.second }.convertToRealUsageTime(),
                                list = item,
                                convertText = { it.convertToRealUsageMinutes() }
                            )
                        }
                    }
                }

//                Spacer(modifier = Modifier.height(32.dp))
//
//                if (appForegroundUsedPagingData != null && appForegroundUsedPagingData.itemCount != 0) {
//                    HorizontalPager(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        state = appForegroundPagerState,
//                        reverseLayout = true,
//                        userScrollEnabled = true,
//                        key = appUsedPagingData?.itemKey { it.first }
//                    ) { page ->
//                        val item = appForegroundUsedPagingData[page]?.second
//                        if (item != null) {
//                            UsageChart(
//                                title = "포그라운드 실행 시간 " +
//                                        item.sumOf { it.second }
//                                            .convertToRealUsageTime(),
//                                list = item,
//                                convertText = { it.convertToRealUsageMinutes() }
//                            )
//                        }
//                    }
//                }

                Spacer(modifier = Modifier.height(32.dp))

                if (appNotifyPagingData != null && appNotifyPagingData.itemCount != 0) {
                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = appNotifyPagerState,
                        reverseLayout = true,
                        userScrollEnabled = true,
                        key = appNotifyPagingData.itemKey { it.first }
                    ) { page ->
                        val item = appNotifyPagingData[page]?.second
                        if (item != null) {
                            UsageChart(
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
                        state = appLaunchPagerState,
                        reverseLayout = true,
                        userScrollEnabled = true,
                        key = appLaunchPagingData.itemKey { it.first }
                    ) { page ->
                        val item = appLaunchPagingData[page]?.second
                        if (item != null) {
                            UsageChart(
                                title = "총 실행 횟수 ${item.sumOf { it.second }}회",
                                list = item,
                                convertText = { "${it}회" }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}