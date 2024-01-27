package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.presentation.screen.common.UsageLaunchCountChart
import com.chs.yourapphistory.presentation.screen.common.UsageTimeZoneChart
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppUsageDetailScreen(viewModel: AppUsageDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val pagerState = if (state.datesList.isNotEmpty()) {
        rememberPagerState(
            pageCount = {
                state.datesList.size
            }, initialPage = state.datesList.indexOf(state.targetDate)
        )
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    LaunchedEffect(state.targetDate) {
        if (state.targetDate != null) {
            viewModel.getDayAppUsageList(state.targetDate!!)
        }
    }


    if (state.datesList.isNotEmpty()) {
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { idx ->
                viewModel.changeDate(state.datesList[idx])
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (!state.targetPackageLabel.isNullOrEmpty()) {
            Text(
                modifier = Modifier
                    .padding(start = 8.dp),
                text = state.targetPackageLabel!!,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }


        if (state.targetDate != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = if (state.targetDate == LocalDate.now()) {
                        "오늘"
                    } else {
                        state.targetDate!!.format(Constants.DATE_FORMAT)
                    },
                    textAlign = TextAlign.Center
                )
            }
        }


        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
            userScrollEnabled = true,
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (state.dayUsageList.isNotEmpty()) {
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        text = state.dayUsageList.sumOf { it.second }.convertToRealUsageTime(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    UsageTimeZoneChart(state.dayUsageList)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        text = "총 실행 횟수 ${state.launchCount.sumOf { it.second }}회",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    UsageLaunchCountChart(state.launchCount)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        text = "포그라운드 실행 시간 " +
                                state.foregroundUsageList.sumOf { it.second }
                                    .convertToRealUsageTime(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    UsageTimeZoneChart(state.foregroundUsageList)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        text = "총 알림 횟수 ${state.notifyCount.sumOf { it.second }}회",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    UsageLaunchCountChart(state.notifyCount)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}