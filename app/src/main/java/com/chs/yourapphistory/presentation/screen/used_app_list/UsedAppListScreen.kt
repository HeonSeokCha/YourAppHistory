package com.chs.yourapphistory.presentation.screen.used_app_list

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.presentation.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsedAppListScreenScreen(
    navController: NavHostController,
    viewModel: UsedAppListViewModel = hiltViewModel()
) {
    val context: Context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingData = state.appInfoList?.collectAsLazyPagingItems()

    val pagerState = rememberPagerState(pageCount = {
        pagingData?.itemCount ?: 0
    })

    LaunchedEffect(context, viewModel) {
        viewModel.insertInfo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
            userScrollEnabled = true
        ) { page ->
            if (pagingData != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        Row {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically),
                                text = pagingData[page]?.first.toString(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    items(count = pagingData[page]?.second?.size ?: 0) { idx ->
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
        }
    }
}

