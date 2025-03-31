package com.chs.yourapphistory.presentation.screen.used_app_list

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val event by viewModel.usedAppEvent.collectAsStateWithLifecycle(UsedAppEvent.Idle)
    val context = LocalContext.current

    LaunchedEffect(event) {
        when (event) {
            UsedAppEvent.Load.Error -> {
                Toast.makeText(context, "Something error in load Data..", Toast.LENGTH_SHORT).show()
            }

            is UsedAppEvent.Click.Application -> {
                (event as UsedAppEvent.Click.Application).run {
                    onClickApp(appInfo, targetDate)
                }
            }

            else -> Unit
        }
    }

    UsedAppListScreenScreen(
        state = state,
        onEvent = viewModel::changeEvent
    )
}

@Composable
fun UsedAppListScreenScreen(
    state: UsedAppListState,
    onEvent: (UsedAppEvent) -> Unit
) {
    val pagingData = state.appInfoList?.collectAsLazyPagingItems()
    val pagerState =
        rememberPagerState(pageCount = { pagingData?.itemCount ?: Constants.NUMBER_LOADING_COUNT })

    if (pagingData != null) {
        when (pagingData.loadState.refresh) {
            is LoadState.Loading -> Unit

            is LoadState.Error -> {
                onEvent(UsedAppEvent.Load.Error)
            }

            else -> {
                onEvent(UsedAppEvent.Load.Complete)
            }
        }

        when (pagingData.loadState.append) {
            is LoadState.Loading -> {
                onEvent(UsedAppEvent.Load.Appending)
            }

            is LoadState.Error -> {
                onEvent(UsedAppEvent.Load.Error)
            }

            else -> {
                onEvent(UsedAppEvent.Load.Complete)
            }
        }

        LaunchedEffect(pagerState.currentPage) {
            if (pagingData.itemCount != 0) {
                onEvent(
                    UsedAppEvent.Click.ChangeDate(pagingData[pagerState.currentPage]!!.first)
                )
            }
        }
    }

    ItemPullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onEvent(UsedAppEvent.Click.RefreshAppUsageInfo) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .placeholder(
                        visible = state.isLoading,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                text = state.displayDate,
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                modifier = Modifier
                    .clickable { onEvent(UsedAppEvent.Click.Sort) }
                    .placeholder(
                        visible = state.isLoading,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                text = state.sortOption.name,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )

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
                    if (state.isLoading) {
                        items(10) {
                            ItemAppInfoSmall(null, null) { }
                        }
                    } else {
                        val packageList = pagingData!![page]!!.second
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
                                    UsedAppEvent.Click.Application(
                                        appInfo = appInfo,
                                        targetDate = pagingData[page]!!.first.toMillis()
                                    )
                                )
                            }
                        }

                        if (state.isAppending) {
                            items(10) {
                                ItemAppInfoSmall(null, null) { }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.isShowFilterDialog) {
        FilterDialog(
            list = state.sortList,
            onDismiss = {
                onEvent(UsedAppEvent.Click.Sort)
            }, onClick = { selectSortType ->
                onEvent(selectSortType)
            }
        )
    }
}
