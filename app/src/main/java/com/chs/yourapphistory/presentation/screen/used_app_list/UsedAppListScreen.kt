package com.chs.yourapphistory.presentation.screen.used_app_list

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.common.CircleLoadingIndicator

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsedAppListScreenScreen(
    navController: NavHostController,
    viewModel: UsedAppListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingData = state.appInfoList?.collectAsLazyPagingItems()

    val pagerState = rememberPagerState(pageCount = {
        pagingData?.itemCount ?: 0
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (pagingData != null && pagingData.itemCount != 0) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = pagingData[pagerState.currentPage]?.first.toString(),
                    textAlign = TextAlign.Center
                )
            }

            HorizontalPager(
                state = pagerState,
                reverseLayout = true,
                userScrollEnabled = true,
                key = { pagingData[it]!!.first }
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(
                        count = pagingData[page]?.second?.size ?: 0,
                        key = {
                            pagingData[page]?.second?.get(it)?.first?.packageName.toString()
                        }
                    ) { idx ->
                        val appInfo = pagingData[page]?.second?.get(idx)
                        if (appInfo != null) {
                            ItemAppInfoSmall(
                                usedAppInfo = appInfo,
                            ) { packageName ->
                                navController.navigate(
                                    "${Screen.ScreenAppUsageDetail.route}/${packageName}/${pagingData[page]?.first?.toMillis()}"
                                )
                            }
                        }
                    }
                }
            }
        } else {
            CircleLoadingIndicator()
        }
    }
}

