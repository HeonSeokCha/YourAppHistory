package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.presentation.screen.common.DailyUsageChart

@Composable
fun ItemDailyPagingInfo(
    title: String,
    item: LazyPagingItems<Map<SortType, List<Pair<Int, Int>>>>
) {
//    DailyUsageChart(
//        title = item.sumOf { it.second }.convertToRealUsageTime(),
//        list = item,
//        convertText = { it.convertToRealUsageMinutes() }
//    )
}