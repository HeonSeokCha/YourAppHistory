package com.chs.yourapphistory.presentation.screen.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.domain.model.AppUsageInfo
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun ItemVerticalChart(
    usageList: List<AppUsageInfo>,
    onSelected: (LocalDateTime) -> Unit
) {
    val density = LocalDensity.current
    val height = with(density) { 300.dp.toPx() }
    var selectIdx by rememberSaveable { mutableIntStateOf(0) }
    val scrollState = rememberLazyListState()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                repeat(3) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(0f, ((100.dp.toPx() * (it) + 10))),
                        end = Offset(size.width, ((100.dp.toPx() * (it) + 10)))
                    )
                }
            },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        reverseLayout = true,
        state = scrollState
    ) {
        itemsIndexed(usageList) { idx, item ->
            Column(
                modifier = Modifier
                    .drawBehind {
                        if (selectIdx == idx) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.Gray,
                                        Color.Transparent
                                    )
                                ),
                                topLeft = Offset(0f, 0f),
                                size = Size(size.width, size.height)
                            )
                        }
                    }
                    .clickable {
                        selectIdx = idx
                        onSelected(item.beginUseTime)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                ItemVerticalCharBar(barHeight = (item.endUseTime - item.beginUseTime).times(scaleValue).toFloat())
//
//                ItemTimeInfo(
//                    date = date,
//                    isFocused = selectIdx == idx
//                )
            }
        }
    }
}

@Composable
fun ItemVerticalCharBar(barHeight: Float) {
    Canvas(
        modifier = Modifier
            .width(30.dp)
            .height(300.dp)
    ) {
        drawRect(
            color = Color.LightGray,
            topLeft = Offset(3.dp.toPx(), 300.dp.toPx() - barHeight),
            size = Size(24.dp.toPx(), barHeight),
        )
    }
}