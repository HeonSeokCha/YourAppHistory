package com.chs.yourapphistory.presentation.screen.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.calculateScale
import com.chs.yourapphistory.common.calculateSplitHourUsage
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.domain.model.AppUsageInfo
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun ItemVerticalChart(
    hourUsageList: List<Pair<Int, Long>>,
    onSelected: (Long) -> Unit
) {
    val density = LocalDensity.current
    val textSize = with(density) { 10.sp.toPx() }
    val smallPadding = with(density) { 4.dp.toPx() }
    val labelSectionHeight = smallPadding.times(2) + textSize
    val textMeasurer = rememberTextMeasurer()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(
                start = 16.dp,
                end = 16.dp
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            repeat(3) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, ((100.dp.toPx() * (it) + 10))),
                    end = Offset(size.width, ((100.dp.toPx() * (it) + 10)))
                )
            }
            val scale = calculateScale(
                (size.height - smallPadding).roundToInt(),
                hourUsageList.map { it.second }
            )
            val chartAreaBottom = size.height - labelSectionHeight

            hourUsageList.forEachIndexed { idx, pair ->
                val barHeight = pair.second.times(scale).toFloat()
                drawRoundRect(
                    color = Color.LightGray,
                    topLeft = Offset(
                        x = 6.dp.toPx() + 14.dp.toPx().times(idx) - 6.dp.toPx().div(2),
                        y = size.height - barHeight - smallPadding - labelSectionHeight
                    ),
                    size = Size(6.dp.toPx(), barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                if (pair.first % 6 == 0) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${pair.first}",
                        topLeft = Offset(
                            x = 6.dp.toPx() + 14.dp.toPx().times(idx) - 6.dp.toPx() / 2,
                            y = chartAreaBottom
                        )
                    )
                }
            }
        }
    }

//    LazyRow(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(
//                start = 16.dp,
//                end = 16.dp
//            )
//            .drawBehind {
//                repeat(3) {
//                    drawLine(
//                        color = Color.Gray,
//                        start = Offset(0f, ((100.dp.toPx() * (it) + 10))),
//                        end = Offset(size.width, ((100.dp.toPx() * (it) + 10)))
//                    )
//                }
//            },
//        horizontalArrangement = Arrangement.spacedBy(5.dp),
//        reverseLayout = true,
//        state = scrollState
//    ) {
//        itemsIndexed(hourUsageList) { idx, item ->
//            Column(
//                modifier = Modifier
//                    .drawBehind {
//                        if (selectIdx == idx) {
//                            drawRoundRect(
//                                brush = Brush.verticalGradient(
//                                    listOf(
//                                        Color.Gray,
//                                        Color.Transparent
//                                    )
//                                ),
//                                topLeft = Offset(0f, 0f),
//                                size = Size(size.width, size.height)
//                            )
//                        }
//                    }
//                    .clickable {
//                        selectIdx = idx
//                        onSelected(hourUsageList[idx].second)
//                    },
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                if (idx % 6 == 0) {
//                    Text(text = (item.first + 1).toString())
//                }
////
////                ItemTimeInfo(
////                    date = date,
////                    isFocused = selectIdx == idx
////                )
//            }
//        }
//    }
}
