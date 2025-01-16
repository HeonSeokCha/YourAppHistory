package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppDetailInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class AppUsageDetailState(
    val displayDate: LocalDate = LocalDate.now(),
    val displayWeek: List<LocalDate> = listOf(),
    val minDate: LocalDate = LocalDate.now(),
    val isDailyMode: Boolean = true,
    val dateList: List<List<LocalDate>> = listOf(),
    val weekList: List<List<LocalDate>> = listOf(),
    val pagingDailyUsedInfo: Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>? = null,
    val pagingDailyForegroundUsedInfo: Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>? = null,
    val pagingDailyNotifyInfo: Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>? = null,
    val pagingDailyLaunchInfo: Flow<PagingData<Pair<LocalDate, List<Pair<Int, Int>>>>>? = null,
    val pagingWeeklyUsedInfo: Flow<PagingData<Pair<List<LocalDate>, List<Pair<String, Int>>>>>? = null,
)
