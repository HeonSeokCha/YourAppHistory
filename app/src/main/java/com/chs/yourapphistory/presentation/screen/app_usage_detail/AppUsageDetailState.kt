package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppDetailInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class AppUsageDetailState(
    val displayDate: LocalDate = LocalDate.now(),
    val minDate: LocalDate = LocalDate.now(),
    val isDailyMode: Boolean = true,
    val dateList: List<List<LocalDate>> = listOf(),
    val weekList: List<List<String>> = listOf(),
    val pagingUsedInfo: Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>? = null,
    val pagingForegroundUsedInfo: Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>? = null,
    val pagingNotifyInfo: Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>? = null,
    val pagingLaunchInfo: Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>? = null
)
