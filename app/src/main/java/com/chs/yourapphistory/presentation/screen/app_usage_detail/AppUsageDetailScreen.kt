package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.presentation.screen.common.UsageChart
import java.time.LocalDate

@Composable
fun AppUsageDetailScreen(
    state: AppUsageDetailState,
    onBack: ()-> Unit
) {

    val pagerState = if (state.datesList.isNotEmpty()) {
        rememberPagerState(
            pageCount = {
                state.datesList.size
            }, initialPage = state.datesList.indexOf(state.targetDate)
        )
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

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
                        state.targetDate.format(Constants.DATE_FORMAT)
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
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
                    UsageChart(
                        title = state.dayUsageList.sumOf { it.second }.convertToRealUsageTime(),
                        list = state.dayUsageList,
                        convertText = { it.convertToRealUsageMinutes() }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    UsageChart(
                        title = "포그라운드 실행 시간 " +
                                state.foregroundUsageList.sumOf { it.second }
                                    .convertToRealUsageTime(),
                        list = state.foregroundUsageList,
                        convertText = { it.convertToRealUsageMinutes() }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    UsageChart(
                        title = "알림 ${state.notifyCount.sumOf { it.second }}개",
                        list = state.notifyCount,
                        convertText = { "${it}개" }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    UsageChart(
                        title = "총 실행 횟수 ${state.launchCount.sumOf { it.second }}회",
                        list = state.launchCount,
                        convertText = { "${it}회" }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}