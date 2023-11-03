package com.example.yourapphistory.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.yourapphistory.common.convertToRealUsageTime
import com.example.yourapphistory.common.toMillis
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun ItemAppInfoSmall(
    appUsagePair: Pair<AppInfo, List<AppUsageInfo>>,
    clickAble: (String) -> Unit
) {
    val appInfo = appUsagePair.first
    val appUsageInfoList = appUsagePair.second
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { clickAble(appInfo.packageName) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            painter = rememberDrawablePainter(drawable = appInfo.icon),
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = appInfo.label
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = appUsageInfoList.sumOf {
                it.endUseTime.toMillis() - it.beginUseTime.toMillis()
            }.convertToRealUsageTime()
        )
    }
}