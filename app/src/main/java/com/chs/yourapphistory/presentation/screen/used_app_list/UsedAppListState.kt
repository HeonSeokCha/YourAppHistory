package com.chs.yourapphistory.presentation.screen.used_app_list

import android.graphics.Bitmap
import androidx.paging.PagingData
import com.chs.yourapphistory.data.model.UsageEventType
import com.chs.yourapphistory.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class UsedAppListState(
    val appInfoList: Flow<PagingData<Pair<LocalDate, List<Pair<AppInfo, String>>>>>? = null,
    val appIconList: HashMap<String, Bitmap?> = hashMapOf(),
    val sortOption: UsageEventType = UsageEventType.AppUsageEvent
)