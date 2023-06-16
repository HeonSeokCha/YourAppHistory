package com.example.yourapphistory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun ItemAppInfoSmall(appInfo: AppInfo) {
    Row {
        Image(
            painter = rememberDrawablePainter(drawable = appInfo.appIcon),
            contentDescription = null
        )

        Text(
            text = appInfo.appLabel
        )

        Text(
            text = appInfo.todayUsageTime.toString()
        )
    }
}