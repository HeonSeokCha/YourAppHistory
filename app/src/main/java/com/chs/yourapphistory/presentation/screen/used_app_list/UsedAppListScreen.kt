package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.model.UsageEventType
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.common.FilterDialog
import com.chs.yourapphistory.presentation.screen.common.PlaceholderHighlight
import com.chs.yourapphistory.presentation.screen.common.placeholder
import com.chs.yourapphistory.presentation.screen.common.shimmer
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun UsedAppListScreenScreen(
    state: UsedAppListState,
    onEvent: (UsageEventType) -> Unit,
    selectPackageLabel: (String) -> Unit,
    onNavigate: (Screen.ScreenAppUsageDetail) -> Unit
) {
    val pagingData = state.appInfoList?.collectAsLazyPagingItems()
    var filterDialogShow by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { pagingData?.itemCount ?: Constants.NUMBER_LOADING_COUNT })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (pagingData != null) {

            Row {
                if (pagingData.itemCount != 0) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterVertically),
                        text = pagingData[pagerState.currentPage]?.first.run {
                            if (this == LocalDate.now()) {
                                "오늘"
                            } else this.toString()
                        },
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp
                    )
                } else {
                    when (pagingData.loadState.refresh) {
                        is LoadState.Loading -> {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterVertically)
                                    .placeholder(
                                        visible = true,
                                        highlight = PlaceholderHighlight.shimmer()
                                    ),
                                text = Constants.TEXT_TITLE_PREVIEW,
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp
                            )
                        }
                        else -> Unit
                    }
                }
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
                key = pagingData.itemKey { it.first }
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
                                icon = state.appIconList[appInfo.first.packageName],
                                sortOption = state.sortOption
                            ) { packageName ->
                                selectPackageLabel(appInfo.first.label)
                                onNavigate(
                                    Screen.ScreenAppUsageDetail(
                                        targetPackageName = packageName,
                                        targetDate = pagingData[page]!!.first.toMillis()
                                    )
                                )
                            }
                        }
                    }
                }
            }


            when (pagingData.loadState.append) {
                is LoadState.Loading -> {

                }

                is LoadState.Error -> {

                }

                else -> Unit
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
