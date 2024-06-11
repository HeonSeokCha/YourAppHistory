package com.chs.yourapphistory.presentation.screen.used_app_list

import android.graphics.Bitmap
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.presentation.screen.common.PlaceholderHighlight
import com.chs.yourapphistory.presentation.screen.common.placeholder
import com.chs.yourapphistory.presentation.screen.common.shimmer

@Composable
fun ItemAppInfoSmall(
    usedAppInfo: Pair<AppInfo, Int>?,
    icon: Bitmap?,
    clickAble: (String) -> Unit
) {
    val appInfo = usedAppInfo?.first
    val dayAppUsedTime = usedAppInfo?.second

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 8.dp,
                    horizontal = 12.dp
                )
                .clickable {
                    if (appInfo != null) {
                        clickAble(appInfo.packageName)
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Image(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        bitmap = icon.asImageBitmap(),
                        contentDescription = null
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        imageVector = Icons.Filled.Android,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        modifier = Modifier
                            .placeholder(
                                visible = appInfo == null,
                                highlight = PlaceholderHighlight.shimmer()
                            ),
                        text = appInfo?.label ?: Constants.TEXT_TITLE_PREVIEW,
                        fontSize = 18.sp,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${dayAppUsedTime ?: Constants.TEXT_TITLE_PREVIEW}",
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}