package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class AppUsageDetailState(
    val dayUsageList: Flow<PagingData<Pair<LocalDate, List<AppUsageInfo>>>>? = null,
)
