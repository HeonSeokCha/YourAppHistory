package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.toConvertDisplayYearDate
import com.chs.yourapphistory.common.toDisplayYearDate

@Composable
fun ItemDateHeader(
    state: AppUsageDetailState,
    onIntent: (AppUsageDetailIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(0.7f),
            text = if (state.isWeeklyMode) {
                state.displayWeek.toDisplayYearDate()
            } else {
                state.displayDate.toConvertDisplayYearDate()
            },
            fontSize = 16.sp,
            maxLines = 2
        )

        Spacer(modifier = Modifier
            .width(8.dp)
            .weight(0.1f))

        Text(
            modifier = Modifier
                .weight(0.2f)
                .clickable { onIntent(AppUsageDetailIntent.OnChangeViewType) },
            text = if (state.isWeeklyMode) {
                "일별 보기"
            } else {
                "주별 보기"
            },
            fontSize = 16.sp
        )
    }
}