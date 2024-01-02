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
import androidx.paging.compose.collectAsLazyPagingItems
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.calculateTimeZoneUsage
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.presentation.screen.common.CircleLoadingIndicator
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
    val pagingData = state.dayUsageList?.collectAsLazyPagingItems()
    var selectHourUsageTime by remember { mutableStateOf("") }

    val pagerState = rememberPagerState(pageCount = { pagingData?.itemCount ?: 0 })

    LaunchedEffect(context, viewModel) {
        viewModel.getDayAppUsageList(packageName, date)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (pagingData != null && pagingData.itemCount != 0) {
            Row {
                val date: LocalDate = pagingData[pagerState.currentPage]?.first!!
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = if (date == LocalDate.now()) {
                        "오늘"
                    } else {
                        date.format(Constants.DATE_FORMAT)
                    },
                    textAlign = TextAlign.Center
                )
            }

            HorizontalPager(
                state = pagerState,
                reverseLayout = true,
                userScrollEnabled = true,
                key = {
                    pagingData[it]!!.first
                }
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val data = pagingData[page]
                    if (data != null) {
                        val date = data.first
                        val dayUsageList = data.second
                        Text(
                            text = dayUsageList.sumOf { (it.endUseTime.toMillis() - it.beginUseTime.toMillis()) }
                                .convertToRealUsageTime(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        ItemVerticalChart(
                            calculateTimeZoneUsage(
                                date = date,
                                list = dayUsageList
                            )
                        ) {
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
                            text = "총 실행 횟수 ${dayUsageList.size}회",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        } else {
            CircleLoadingIndicator()
        }
    }
}