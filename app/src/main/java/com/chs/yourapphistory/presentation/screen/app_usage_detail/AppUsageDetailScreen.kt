package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.toConvertDisplayYearDate
import com.chs.yourapphistory.common.toDisplayYearDate
import com.chs.yourapphistory.domain.model.SortType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDate

@Composable
fun AppUsageDetailScreenRoot(
    viewModel: AppUsageDetailViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dailyPagingItems = viewModel.getDailyPagingData.collectAsLazyPagingItems()
    val weeklyPagingItems = viewModel.getWeeklyPagingData.collectAsLazyPagingItems()

    AppUsageDetailScreen(
        state = state,
        dailyPagingItems = dailyPagingItems,
        weeklyPagingItems = weeklyPagingItems,
        onIntent = viewModel::handleIntent
    )
}

@Composable
fun AppUsageDetailScreen(
    state: AppUsageDetailState,
    dailyPagingItems: LazyPagingItems<Map<SortType, List<Pair<Int, Int>>>>,
    weeklyPagingItems: LazyPagingItems<Map<SortType, List<Pair<LocalDate, Int>>>>,
    onIntent: (AppUsageDetailIntent) -> Unit
) {
    /* date related variables */
    val datePagerState = if (state.dateList.isNotEmpty()) {
        rememberPagerState(
            pageCount = { state.dateList.count() },
            initialPage = state.dateIdx.first
        )
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    LaunchedEffect(dailyPagingItems.loadState.refresh) {
        when (dailyPagingItems.loadState.refresh) {
            is LoadState.Loading -> onIntent(AppUsageDetailIntent.DateLoading)
            is LoadState.NotLoading -> onIntent(AppUsageDetailIntent.DateLoadComplete)
            is LoadState.Error -> onIntent(AppUsageDetailIntent.Error)
        }
    }

    LaunchedEffect(datePagerState.currentPage, datePagerState.isScrollInProgress) {
        if (state.dateList.isEmpty()) return@LaunchedEffect
        if (datePagerState.currentPageOffsetFraction != 0f) return@LaunchedEffect
        if (datePagerState.isScrollInProgress) return@LaunchedEffect

        onIntent(
            AppUsageDetailIntent.OnChangeTargetDateIdx(datePagerState.currentPage to state.dateIdx.second)
        )
    }

    val dailyUsagePager = if (state.isDateLoading) {
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
    } else {
        val initIdx = state.dateList.flatten().run {
            this.indexOf(state.displayDate) - this.indexOf(LocalDate.now())
        }
        if (initIdx > dailyPagingItems.itemCount) {
            rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
        } else {
            rememberPagerState(initialPage = initIdx, pageCount = { dailyPagingItems.itemCount })
        }
    }

    val dailyForegroundUsagePager = if (state.isDateLoading) {
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
    } else {
        val initIdx = state.dateList.flatten().run {
            this.indexOf(state.displayDate) - this.indexOf(LocalDate.now())
        }
        if (initIdx > dailyPagingItems.itemCount) {
            rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
        } else {
            rememberPagerState(initialPage = initIdx, pageCount = { dailyPagingItems.itemCount })
        }
    }

    val dailyNotifyPager = if (state.isDateLoading) {
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
    } else {
        val initIdx = state.dateList.flatten().run {
            this.indexOf(state.displayDate) - this.indexOf(LocalDate.now())
        }
        if (initIdx > dailyPagingItems.itemCount) {
            rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
        } else {
            rememberPagerState(initialPage = initIdx, pageCount = { dailyPagingItems.itemCount })
        }
    }

    val dailyLaunchPager = if (state.isDateLoading) {
        rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
    } else {
        val initIdx = state.dateList.flatten().run {
            this.indexOf(state.displayDate) - this.indexOf(LocalDate.now())
        }
        if (initIdx > dailyPagingItems.itemCount) {
            rememberPagerState(initialPage = 0, pageCount = { dailyPagingItems.itemCount })
        } else {
            rememberPagerState(initialPage = initIdx, pageCount = { dailyPagingItems.itemCount })
        }
    }

    LaunchedEffect(state.dateIdx) {
        chsLog("changeDate" + state.dateIdx.toString())
        val page = state.dateIdx.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            (this.first * 7 + this.second) - initIdx
        }
        if (datePagerState.currentPage != state.dateIdx.first) {
            datePagerState.scrollToPage(state.dateIdx.first)
        }
        awaitAll(
            async { dailyForegroundUsagePager.scrollToPage(page) },
            async { dailyUsagePager.scrollToPage(page) },
            async { dailyNotifyPager.scrollToPage(page) },
            async { dailyLaunchPager.scrollToPage(page) }
        )
    }

    LaunchedEffect(dailyUsagePager.currentPage, dailyUsagePager.isScrollInProgress) {
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        if (dailyUsagePager.isScrollInProgress) return@LaunchedEffect
        val idx = dailyUsagePager.currentPage.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            ((initIdx + this) / 7) to (initIdx + this) % 7
        }
        onIntent(AppUsageDetailIntent.OnChangeTargetDateIdx(idx))
    }

    LaunchedEffect(
        dailyForegroundUsagePager.currentPage,
        dailyForegroundUsagePager.isScrollInProgress
    ) {
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        if (dailyForegroundUsagePager.isScrollInProgress) return@LaunchedEffect
        val idx = dailyForegroundUsagePager.currentPage.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            ((initIdx + this) / 7) to (initIdx + this) % 7
        }
        onIntent(AppUsageDetailIntent.OnChangeTargetDateIdx(idx))
    }

    LaunchedEffect(dailyNotifyPager.currentPage, dailyNotifyPager.isScrollInProgress) {
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        if (dailyNotifyPager.isScrollInProgress) return@LaunchedEffect
        val idx = dailyNotifyPager.currentPage.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            ((initIdx + this) / 7) to (initIdx + this) % 7
        }
        onIntent(AppUsageDetailIntent.OnChangeTargetDateIdx(idx))
    }

    LaunchedEffect(dailyLaunchPager.currentPage, dailyLaunchPager.isScrollInProgress) {
        if (state.dateList.isEmpty() || state.isDateLoading) return@LaunchedEffect
        if (dailyLaunchPager.isScrollInProgress) return@LaunchedEffect
        val idx = dailyLaunchPager.currentPage.run {
            val initIdx = state.dateList.flatten().indexOf(LocalDate.now())
            ((initIdx + this) / 7) to (initIdx + this) % 7
        }
        onIntent(AppUsageDetailIntent.OnChangeTargetDateIdx(idx))
    }
    /* date related variables end*/

    /* week related variables */
    val weekPagerState = if (state.weekList.isNotEmpty()) {
        rememberPagerState(pageCount = { state.weekList.count() })
    } else {
        rememberPagerState(pageCount = { 0 })
    }

    LaunchedEffect(weeklyPagingItems.loadState.refresh) {
        when (weeklyPagingItems.loadState.refresh) {
            is LoadState.Loading -> onIntent(AppUsageDetailIntent.WeekLoading)
            is LoadState.NotLoading -> onIntent(AppUsageDetailIntent.WeekLoadComplete)
            is LoadState.Error -> onIntent(AppUsageDetailIntent.Error)
        }
    }

    LaunchedEffect(weekPagerState.currentPage, weekPagerState.isScrollInProgress) {
        if (state.weekList.isEmpty()) return@LaunchedEffect
        if (weekPagerState.isScrollInProgress) return@LaunchedEffect
        onIntent(
            AppUsageDetailIntent.OnChangeTargetWeekIdx(weekPagerState.currentPage to state.weekIdx.second)
        )
    }

    val weeklyUsagePager =
        rememberPagerState(initialPage = 0, pageCount = { weeklyPagingItems.itemCount })

    val weeklyForegroundUsagePager =
        rememberPagerState(initialPage = 0, pageCount = { weeklyPagingItems.itemCount })

    val weeklyNotifyPager =
        rememberPagerState(initialPage = 0, pageCount = { weeklyPagingItems.itemCount })

    val weeklyLaunchPager =
        rememberPagerState(initialPage = 0, pageCount = { weeklyPagingItems.itemCount })

    LaunchedEffect(state.weekIdx) {
        weekPagerState.scrollToPage(state.weekIdx.first)
        awaitAll(
            async { weeklyUsagePager.scrollToPage(state.weekIdx.second) },
            async { weeklyForegroundUsagePager.scrollToPage(state.weekIdx.second) },
            async { weeklyNotifyPager.scrollToPage(state.weekIdx.second) },
            async { weeklyLaunchPager.scrollToPage(state.weekIdx.second) }
        )
    }

    LaunchedEffect(weeklyUsagePager.currentPage, weeklyUsagePager.isScrollInProgress) {
        if (state.weekList.isEmpty() || state.isWeekLoading) return@LaunchedEffect
        if (weeklyUsagePager.isScrollInProgress) return@LaunchedEffect
        onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(state.weekIdx.first to weeklyUsagePager.currentPage))
    }

    LaunchedEffect(
        weeklyForegroundUsagePager.currentPage,
        weeklyForegroundUsagePager.isScrollInProgress
    ) {
        if (state.weekList.isEmpty() || state.isWeekLoading) return@LaunchedEffect
        if (weeklyForegroundUsagePager.isScrollInProgress) return@LaunchedEffect
        onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(state.weekIdx.first to weeklyForegroundUsagePager.currentPage))
    }

    LaunchedEffect(weeklyNotifyPager.currentPage, weeklyNotifyPager.isScrollInProgress) {
        if (state.weekList.isEmpty() || state.isWeekLoading) return@LaunchedEffect
        if (weeklyNotifyPager.isScrollInProgress) return@LaunchedEffect
        onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(state.weekIdx.first to weeklyNotifyPager.currentPage))
    }

    LaunchedEffect(weeklyLaunchPager.currentPage, weeklyLaunchPager.isScrollInProgress) {
        if (state.weekList.isEmpty() || state.isWeekLoading) return@LaunchedEffect
        if (weeklyLaunchPager.isScrollInProgress) return@LaunchedEffect
        onIntent(AppUsageDetailIntent.OnChangeTargetWeekIdx(state.weekIdx.first to weeklyLaunchPager.currentPage))
    }

    /* week related variables end*/

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (state.isWeeklyMode) {
                Text(
                    text = state.displayWeek.toDisplayYearDate(),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            } else {
                Text(
                    text = if (state.displayDate == LocalDate.now()) {
                        "오늘"
                    } else {
                        state.displayDate.toConvertDisplayYearDate()
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }

            Text(
                modifier = Modifier
                    .clickable { onIntent(AppUsageDetailIntent.OnChangeViewType) },
                text = if (state.isWeeklyMode) {
                    "일별 보기"
                } else {
                    "주별 보기"
                },
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }

        if (state.isWeeklyMode) {
            ItemWeekList(
                state = weekPagerState,
                targetWeek = state.displayWeek,
                item = state.weekList,
                onIntent = onIntent
            )
        } else {
            ItemDateList(
                state = datePagerState,
                minDate = state.minDate,
                targetDate = state.displayDate,
                item = state.dateList,
                onIntent = onIntent
            )
        }

        if (state.isWeeklyMode) {
            ItemWeeklyPagingInfo(
                weeklyUsagePager = weeklyUsagePager,
                weeklyForegroundUsagePager = weeklyForegroundUsagePager,
                weeklyNotifyPager = weeklyNotifyPager,
                weeklyLaunchPager = weeklyLaunchPager,
                weeklyPagingItems = weeklyPagingItems
            )
        } else {
            ItemDailyPagingInfo(
                dailyUsagePager = dailyUsagePager,
                dailyForegroundUsagePager = dailyForegroundUsagePager,
                dailyNotifyPager = dailyNotifyPager,
                dailyLaunchPager = dailyLaunchPager,
                dailyPagingItems = dailyPagingItems
            )
        }
    }
}