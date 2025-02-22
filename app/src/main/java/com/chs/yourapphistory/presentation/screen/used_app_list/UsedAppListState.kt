package com.chs.yourapphistory.presentation.screen.used_app_list

import android.graphics.Bitmap
import androidx.paging.PagingData
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class UsedAppListState(
    val isLoading: Boolean = true,
    val displayDate: String = "오늘",
    val appInfoList: Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, Int>>>>>? = null,
    val appIconList: HashMap<String, Bitmap?> = hashMapOf(),
    val sortList: List<UsedAppEvent.GetUsageEvent> = Constants.USAGE_EVENT_TYPE_LIST,
    val sortOption: UsedAppEvent.GetUsageEvent = UsedAppEvent.GetUsageEvent.AppUsageEvent
)