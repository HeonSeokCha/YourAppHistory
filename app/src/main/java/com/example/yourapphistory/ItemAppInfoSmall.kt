package com.example.yourapphistory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun ItemAppInfoSmall(appInfo: AppInfo) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier
                .size(48.dp),
            painter = rememberDrawablePainter(drawable = appInfo.appIcon),
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = appInfo.appLabel
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = appInfo.todayUsageTime.convertToRealUsageTime()
        )
    }
}