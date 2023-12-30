package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.paging.PagingData
import com.chs.yourapphistory.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class UsedAppListState(
    val appInfoList: Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, String>>>>>? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)