package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.presentation.screen.common.PlaceholderHighlight
import com.chs.yourapphistory.presentation.screen.common.UsageChart
import com.chs.yourapphistory.presentation.screen.common.placeholder
import com.chs.yourapphistory.presentation.screen.common.shimmer
import java.time.LocalDate

@Composable
fun AppUsageDetailScreen(
    state: AppUsageDetailState,
    onBack: () -> Unit
) {
    var currentDate by remember { mutableStateOf(state.targetDate) }
    val pagingData = state.pagingDetailInfo?.collectAsLazyPagingItems()
    val pagerState = if (pagingData != null && pagingData.itemCount != 0) {
        rememberPagerState(
            pageCount = {
                pagingData.itemCount
            }, initialPage = pagingData.itemSnapshotList.items.map {
                it.first
            }.indexOf(state.targetDate)
        )
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagingData != null && pagingData.itemCount != 0) {
            currentDate = pagingData[pagerState.currentPage]?.first ?: LocalDate.now()
        }
    }

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier
                    .placeholder(
                        visible = pagingData == null || pagingData.loadState.refresh == LoadState.Loading,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                text = if (pagingData == null || pagingData.itemCount == 0) {
                    Constants.TEXT_TITLE_PREVIEW
                } else {
                    if (currentDate == LocalDate.now()) {
                        "오늘"
                    } else {
                        currentDate.format(Constants.DATE_FORMAT)
                    }
                },
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }


        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
            userScrollEnabled = true,
            key = pagingData?.itemKey { it.first }
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (pagingData != null && pagingData.itemCount != 0) {
                    val item = pagingData[page]?.second

                    if (item != null) {
                        UsageChart(
                            title = item.usageInfo.sumOf { it.second }.convertToRealUsageTime(),
                            list = item.usageInfo,
                            convertText = { it.convertToRealUsageMinutes() }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        UsageChart(
                            title = "포그라운드 실행 시간 " +
                                    item.foregroundUsageInfo.sumOf { it.second }
                                        .convertToRealUsageTime(),
                            list = item.foregroundUsageInfo,
                            convertText = { it.convertToRealUsageMinutes() }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        UsageChart(
                            title = "알림 ${item.notifyInfo.sumOf { it.second }}개",
                            list = item.notifyInfo,
                            convertText = { "${it}개" }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        UsageChart(
                            title = "총 실행 횟수 ${item.launchCountInfo.sumOf { it.second }}회",
                            list = item.launchCountInfo,
                            convertText = { "${it}회" }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                } else {
                    repeat(4) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(horizontal = 8.dp)
                                .placeholder(
                                    visible = true,
                                    highlight = PlaceholderHighlight.shimmer()
                                )
                        ) {}
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}