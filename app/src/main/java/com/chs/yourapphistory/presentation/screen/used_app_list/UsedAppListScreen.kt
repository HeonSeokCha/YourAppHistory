package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.common.CircleLoadingIndicator
import com.chs.yourapphistory.presentation.screen.common.FilterDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsedAppListScreenScreen(
    navController: NavHostController,
    viewModel: UsedAppListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingData = state.appInfoList?.collectAsLazyPagingItems()
    var filterDialogShow by remember { mutableStateOf(false) }
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
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }

            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .clickable {
                           filterDialogShow = true
                        },
                    text = state.sortOption.name,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }

            HorizontalPager(
                state = pagerState,
                reverseLayout = true,
                userScrollEnabled = true,
                key = { pagingData[it]!!.first }
            ) { page ->
                val packageList = pagingData[page]!!.second
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (packageList.isNotEmpty()) {
                        items(
                            count = packageList.size,
                            key = {
                                packageList[it].first.packageName
                            }
                        ) { idx ->
                            val appInfo = packageList[idx]
                            ItemAppInfoSmall(
                                usedAppInfo = appInfo,
                                icon = state.appIconList[appInfo.first.packageName]
                            ) { packageName ->
                                navController.navigate(
                                    "${Screen.ScreenAppUsageDetail.route}/${packageName}/${pagingData[page]!!.first.toMillis()}"
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

    if (filterDialogShow) {
        FilterDialog(
            list = state.sortList,
            onDismiss = {
                filterDialogShow = false
            }, onClick = { selectSortType ->
                viewModel.changeSortOption(selectSortType)
            }
        )
    }
}

