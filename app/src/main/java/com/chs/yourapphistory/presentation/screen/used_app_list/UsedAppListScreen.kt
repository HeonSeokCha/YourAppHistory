package com.chs.yourapphistory.presentation.screen.used_app_list

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chs.yourapphistory.R
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.presentation.screen.common.FilterDialog
import com.chs.yourapphistory.presentation.screen.common.PlaceholderHighlight
import com.chs.yourapphistory.presentation.screen.common.placeholder
import com.chs.yourapphistory.presentation.screen.common.shimmer
import java.time.LocalDate

@Composable
fun UsedAppListScreenScreenRoot(
    viewModel: UsedAppListViewModel,
    onClickApp: (AppInfo, Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pagingItems = viewModel.pagingList.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UsedAppEffect.NavigateAppDetail -> onClickApp(effect.appInfo, effect.targetDate)
                is UsedAppEffect.ShowPagingError -> {
                    Toast.makeText(
                        context,
                        "Something wrong load Used Info..",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    UsedAppListScreenScreen(
        state = state,
        pagingItems = pagingItems,
        onIntent = viewModel::handleIntent
    )
}

@Composable
fun UsedAppListScreenScreen(
    state: UsedAppListState,
    pagingItems: LazyPagingItems<Pair<LocalDate, List<Pair<AppInfo, Int>>>>,
    onIntent: (UsedAppIntent) -> Unit
) {
    val pagerState = rememberPagerState(
        pageCount = {
            pagingItems.itemCount
        }
    )

    val isEmpty by remember {
        derivedStateOf {
            pagingItems.loadState.refresh is LoadState.NotLoading
                    && pagingItems.loadState.append.endOfPaginationReached
                    && pagingItems.itemCount == 0
        }
    }

    LaunchedEffect(pagingItems.loadState.refresh) {
        when (pagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                chsLog("START LOADING")
                pagerState.scrollToPage(0)
                onIntent(UsedAppIntent.Loading)
            }

            is LoadState.Error -> onIntent(UsedAppIntent.Error)

            is LoadState.NotLoading -> onIntent(UsedAppIntent.LoadComplete)
        }
    }

    LaunchedEffect(pagingItems.loadState.append) {
        when (pagingItems.loadState.append) {
            is LoadState.Loading -> onIntent(UsedAppIntent.Appending)

            is LoadState.Error -> onIntent(UsedAppIntent.Error)

            is LoadState.NotLoading -> onIntent(UsedAppIntent.AppendComplete)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (state.isLoading || pagingItems.itemCount == 0) return@LaunchedEffect
        if (pagingItems[pagerState.currentPage]?.first == null) return@LaunchedEffect

        onIntent(UsedAppIntent.ChangeDate(pagingItems[pagerState.currentPage]!!.first))
    }

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
                .clickable {
                    if (state.isLoading) return@clickable
                    onIntent(UsedAppIntent.OnShowSortDialog(true))
                }
                .placeholder(
                    visible = state.isLoading,
                    highlight = PlaceholderHighlight.shimmer()
                ),
            text = state.sortOption.title,
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )

        when {
            state.isLoading -> {
                ItemLoading()
            }

            isEmpty -> {
                ItemEmpty()
            }

            else -> {
                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState,
                    reverseLayout = true,
                    userScrollEnabled = true,
                    key = pagingItems.itemKey { it.first }
                ) { page ->
                    val item = pagingItems[page] ?: return@HorizontalPager

                    if (state.isAppending) {
                        ItemLoading()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val packageList = item.second
                            items(
                                count = packageList.count(),
                                key = { packageList[it].first.packageName }
                            ) { idx ->
                                val appInfo = packageList[idx]
                                ItemAppInfoSmall(
                                    usedAppInfo = appInfo,
                                    icon = appInfo.first.icon,
                                    sortOption = state.sortOption
                                ) { appInfo ->
                                    onIntent(
                                        UsedAppIntent.ClickAppInfo(
                                            appInfo = appInfo,
                                            targetDate = item.first
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.isShowFilterDialog) {
        FilterDialog(
            list = SortType.entries.toList(),
            onDismiss = {
                onIntent(UsedAppIntent.OnShowSortDialog(false))
            }, onClick = { selectSortType ->
                onIntent(UsedAppIntent.OnChangeSort(selectSortType))
            }
        )
    }
}

@Composable
private fun ItemLoading() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(100) {
            ItemAppInfoSmall(null, null) { }
        }
    }
}

@Composable
fun ItemEmpty() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(text = stringResource(R.string.txt_no_item))
    }
}
