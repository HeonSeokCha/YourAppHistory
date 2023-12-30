package com.chs.yourapphistory.presentation.screen.app_usage_detail

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.presentation.screen.common.ItemVerticalChart
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppUsageDetailScreen(
    packageName: String,
    date: LocalDate,
    viewModel: AppUsageDetailViewModel = hiltViewModel()
) {
    val context: Context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectHourUsageTime by remember { mutableStateOf("") }

    val pagerState = if (state.localDateList.isEmpty()) {
        rememberPagerState(
            pageCount = {
                state.localDateList.size
            }, initialPage = 0
        )
    } else {
        rememberPagerState(
            pageCount = {
                state.localDateList.size
            }, initialPage = state.localDateList.indexOf(date)
        )
    }

    LaunchedEffect(state.targetDate) {
        viewModel.getDayAppUsageList(
            packageName = packageName,
            state.targetDate
        )
        selectHourUsageTime = ""
    }

    if (state.localDateList.isNotEmpty()) {
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect {
                viewModel.changeDate(it)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (state.localDateList.isNotEmpty()) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = if (state.targetDate == LocalDate.now()) {
                        "오늘"
                    } else {
                        state.targetDate.format(Constants.DATE_FORMAT)
                    },
                    textAlign = TextAlign.Center
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
            userScrollEnabled = true
        ) { page ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (state.dayUsageList.isNotEmpty()) {
                    item {
                        Text(
                            text = state.dayUsageList.sumOf { it.second }.convertToRealUsageTime(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        ItemVerticalChart(state.dayUsageList) {
                            if (it != null) {
                                selectHourUsageTime =
                                    "${it.first}:00 ~ ${it.first + 1}:00  ->  ${it.second.convertToRealUsageTime()}"
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        if (selectHourUsageTime.isNotEmpty()) {
                            Text(text = selectHourUsageTime)
                        }


                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "총 실행 횟수 ${state.dayLaunchCount}회",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}