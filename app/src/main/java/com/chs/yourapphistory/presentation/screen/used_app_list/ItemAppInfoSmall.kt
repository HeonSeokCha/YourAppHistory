package com.chs.yourapphistory.presentation.screen.used_app_list

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.chs.yourapphistory.domain.model.AppInfo

@Composable
fun ItemAppInfoSmall(
    usedAppInfo: Pair<AppInfo, String>,
    clickAble: (String) -> Unit
) {
    val appInfo = usedAppInfo.first
    val dayAppUsedTime = usedAppInfo.second

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .padding(4.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { clickAble(appInfo.packageName) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appInfo.icon != null) {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        bitmap = appInfo.icon.asImageBitmap(),
                        contentDescription = null
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        imageVector = Icons.Filled.Android,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = appInfo.label
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = dayAppUsedTime)
            }
        }
    }
}