package com.chs.yourapphistory.presentation.screen.used_app_list

import android.graphics.Bitmap
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.domain.model.SortType

data class UsedAppListState(
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val isRefreshing: Boolean = false,
    val isShowFilterDialog: Boolean = false,
    val displayDate: String = "오늘",
    val appIconList: HashMap<String, Bitmap?> = hashMapOf(),
    val sortOption: SortType = SortType.UsageEvent
)