package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppDetailInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class AppUsageDetailState(
    val targetDate: LocalDate = LocalDate.now(),
    val pagingDetailInfo: Flow<PagingData<Pair<LocalDate, AppDetailInfo>>>? = null,
)
