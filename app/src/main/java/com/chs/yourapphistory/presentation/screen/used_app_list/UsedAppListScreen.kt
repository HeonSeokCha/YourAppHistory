package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.common.CircleLoadingIndicator
import com.chs.yourapphistory.presentation.screen.common.FilterDialog
import com.chs.yourapphistory.presentation.screen.common.ItemPullToRefreshBox
import com.chs.yourapphistory.presentation.screen.common.PlaceholderHighlight
import com.chs.yourapphistory.presentation.screen.common.placeholder
import com.chs.yourapphistory.presentation.screen.common.shimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun UsedAppListScreenScreenRoot(
    viewModel: UsedAppListViewModel,
    onClickApp: (AppInfo, Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    UsedAppListScreenScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                is UsedAppEvent.ClickApplication -> {
                    onClickApp(event.appInfo, event.targetDate)
                }
                else -> Unit
            }

            viewModel.changeEvent(event)
        }
    )
}

@Composable
fun UsedAppListScreenScreen(
    state: UsedAppListState,
    onEvent: (UsedAppEvent) -> Unit
) {
    val pagingData = state.appInfoList?.collectAsLazyPagingItems()
    var filterDialogShow by remember { mutableStateOf(false) }
    val pagerState =
        rememberPagerState(pageCount = { pagingData?.itemCount ?: Constants.NUMBER_LOADING_COUNT })
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    ItemPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                onEvent(UsedAppEvent.RefreshAppUsageInfo)
                delay(500L)
                isRefreshing = false
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically)
                        .placeholder(
                            visible = pagingData == null || pagingData.loadState.refresh == LoadState.Loading,
                            highlight = PlaceholderHighlight.shimmer()
                        ),
                    text = if (pagingData == null || pagingData.loadState.refresh == LoadState.Loading) {
                        Constants.TEXT_TITLE_PREVIEW
                    } else {
                        pagingData[pagerState.currentPage]?.first.run {
                            if (this == LocalDate.now()) {
                                "오늘"
                            } else this.toString()
                        }
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp
                )
            }

            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .clickable {
                            filterDialogShow = true
                        }
                        .placeholder(
                            visible = pagingData == null || pagingData.loadState.refresh == LoadState.Loading,
                            highlight = PlaceholderHighlight.shimmer()
                        ),
                    text = state.sortOption.name,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }


            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                reverseLayout = true,
                userScrollEnabled = true,
                key = pagingData?.itemKey { it.first }
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (pagingData != null && pagingData.itemCount != 0) {
                        val packageList = pagingData[page]!!.second
                        items(
                            count = packageList.size,
                            key = { packageList[it].first.packageName }
                        ) { idx ->
                            val usedAppInfo = packageList[idx]
                            ItemAppInfoSmall(
                                usedAppInfo = usedAppInfo,
                                icon = state.appIconList[usedAppInfo.first.packageName],
                                sortOption = state.sortOption
                            ) { appInfo ->
                                onEvent(
                                    UsedAppEvent.ClickApplication(
                                        appInfo = appInfo,
                                        targetDate = pagingData[page]!!.first.toMillis()
                                    )
                                )
                            }
                        }

                        when (pagingData.loadState.refresh) {
                            is LoadState.Loading -> {
                                items(10) {
                                    ItemAppInfoSmall(null, null) { }
                                }
                            }

                            is LoadState.Error -> {
                                item {
                                    Text((pagingData.loadState.append as LoadState.Error).error.message.toString())
                                }
                            }

                            else -> Unit
                        }


                        when (pagingData.loadState.append) {
                            is LoadState.Loading -> {
                                items(10) {
                                    ItemAppInfoSmall(null, null) { }
                                }
                            }

                            is LoadState.Error -> {
                                item {
                                    Text((pagingData.loadState.append as LoadState.Error).error.message.toString())
                                }
                            }

                            else -> Unit
                        }
                    } else {
                        items(10) {
                            ItemAppInfoSmall(null, null) { }
                        }
                    }
                }
            }
        }
    }



    if (filterDialogShow) {
        FilterDialog(
            list = state.sortList,
            onDismiss = {
                filterDialogShow = false
            }, onClick = { selectSortType ->
                coroutineScope.launch {
                    pagerState.scrollToPage(0)
                }
                onEvent(selectSortType)
            }
        )
    }
}
