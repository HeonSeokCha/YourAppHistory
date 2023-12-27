package com.chs.yourapphistory.presentation.screen.used_app_list

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.paging.compose.collectAsLazyPagingItems
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.presentation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        viewModel.getDayUseAppInfoList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (pagingData != null) {
            HorizontalPager(
                state = pagerState,
                reverseLayout = true,
                userScrollEnabled = true,
            ) { page ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    )
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
                    items(
                        items = pagingData[page]?.second ?: emptyList(),
                        key = { it.first.packageName }
                    ) { appInfo ->
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

