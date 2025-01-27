package com.chs.yourapphistory.presentation.screen.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.calculateScale
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertDayString
import com.chs.yourapphistory.common.isZero
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ItemWeeklyChart(
    weekUsageList: List<Pair<String, Int>>,
    clickText: DrawScope.(
        TextMeasurer,
        BarArea,
    ) -> Unit
) {
    val density = LocalDensity.current
    val textSize = with(density) { 10.sp.toPx() }
    val smallPadding = with(density) { 4.dp.toPx() }
    val labelSectionHeight = smallPadding.times(2) + textSize
    val topBasePadding = with(density) { 14.sp.toPx() + 21f }
    val barWidth = with(density) { 12.dp.toPx() }
    val distance = with(density) {
        (LocalConfiguration.current.screenWidthDp - 25).div(7).dp.toPx()
    }
    val barColor = MaterialTheme.colorScheme.primary

    val textMeasurer = rememberTextMeasurer()

    val style1 = TextStyle(
        fontSize = 10.sp,
        color = Color.Black
    )

    val basePadding = textMeasurer
        .measure(
            text = DayOfWeek.SUNDAY.getDisplayName(
                java.time.format.TextStyle.SHORT,
                Locale.KOREAN
            ),
            style = style1
        ).size
        .width
        .toFloat()

    val barAreas = weekUsageList.mapIndexed { idx, pair ->
        chsLog(distance.times(idx).toString())
        BarArea(
            idx = idx,
            value = pair.second,
            xStart = distance.times(idx) + barWidth + smallPadding.times(2),
            xEnd = distance.times(idx) + barWidth + smallPadding.times(2) + barWidth
        )
    }

    var selectedBar: BarArea? by remember { mutableStateOf(null) }
    var selectedPos by remember { mutableFloatStateOf(0f) }


    LaunchedEffect(barAreas) {
        selectedBar = null
    }

    LaunchedEffect(selectedPos) {
        val findBar = barAreas.find { selectedPos in it.xStart..it.xEnd }
        selectedBar = if (findBar?.value.isZero()) {
            null
        } else findBar
    }

    val scope = rememberCoroutineScope()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 4.dp)
            .tapOrPress(
                onStart = { },
                onCancel = { },
                onCompleted = { scope.launch { selectedPos = it } }
            )
    ) {
        if (size.height != 0f) {
            repeat(3) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(basePadding.div(2), ((size.height / 3) * it) + topBasePadding),
                    end = Offset(
                        size.width - basePadding.div(2),
                        ((size.height / 3) * it) + topBasePadding
                    )
                )
            }
        }
        val scale = calculateScale(
            (size.height - smallPadding - topBasePadding).roundToInt(),
            weekUsageList.map { it.second }
        )
        val chartAreaBottom = size.height - labelSectionHeight

        barAreas.forEachIndexed { idx, info ->
            val barHeight = info.value.times(scale).toFloat()

            chsLog(distance.times(idx).toString())
            drawRoundRect(
                color = barColor,
                topLeft = Offset(
                    x = distance.times(info.idx) + barWidth + smallPadding.times(2),
                    y = size.height - barHeight - smallPadding - labelSectionHeight
                ),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            val textResult = textMeasurer.measure(
                text = weekUsageList[info.idx].first,
                style = style1
            )
            val textRectPadding = (distance.times(info.idx)) + (textResult.size.width) + (smallPadding * 3)
            drawText(
                textMeasurer = textMeasurer,
                text = weekUsageList[info.idx].first,
                topLeft = Offset(
                    x = textRectPadding,
                    y = chartAreaBottom
                ),
                style = style1
            )
        }

        if (selectedBar != null) {
            val barHeight = (size.height - selectedBar!!.value.times(scale)
                .toFloat() - smallPadding - labelSectionHeight)

            drawLine(
                color = Color.Black,
                start = Offset(selectedBar!!.xStart + barWidth.div(2), 50f),
                end = Offset(selectedBar!!.xStart + barWidth.div(2), barHeight),
                strokeWidth = 4f
            )

            clickText(
                textMeasurer,
                selectedBar!!,
            )
        }
    }
}

@Composable
fun WeeklyUsageChart(
    title: String,
    subTitle: String,
    list: List<Pair<String, Int>>,
    convertText: (Int) -> String
) {
    val barColor = MaterialTheme.colorScheme.onTertiary
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(
                    top = 8.dp,
                    start = 8.dp
                ),
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            modifier = Modifier
                .padding(
                    top = 8.dp,
                    start = 8.dp
                ),
            text = subTitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        ItemWeeklyChart(weekUsageList = list) { textMeasurer, selectedBar ->
            val selectDayValue: String = selectedBar.idx.convertDayString()
            val selectTimeMeasurer: TextLayoutResult = textMeasurer.measure(
                selectDayValue,
                TextStyle(
                    fontSize = 12.sp,
                    color = Color.Black
                ),
            )


            val selectValue = convertText(selectedBar.value)
            val selectValueMeasurer: TextLayoutResult = textMeasurer.measure(
                selectValue,
                TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
            )
            val totalWidth: Float =
                (selectTimeMeasurer.size.width + selectValueMeasurer.size.width).toFloat()

            val textRectPadding = selectedBar.xStart - ((totalWidth.div(7)) * selectedBar.idx)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(
                    textRectPadding,
                    0f
                ),
                size = Size(
                    totalWidth + 12.dp.toPx(),
                    selectValueMeasurer.size.height.toFloat() + 21f
                ),
                cornerRadius = CornerRadius(50f)
            )

            drawText(
                textMeasurer = textMeasurer,
                text = selectDayValue,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.Black
                ),
                topLeft = Offset(
                    textRectPadding + 20f,
                    12f
                )
            )

            drawText(
                textMeasurer = textMeasurer,
                text = selectValue,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                topLeft = Offset(
                    textRectPadding + 20f + selectTimeMeasurer.size.width.toFloat(),
                    6f
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
private fun PreViewUsageChart2() {
    val usageMap = object : HashMap<String, Int>() {
        init {
            for (i in 0..6) {
                put(
                    DayOfWeek.entries.sortedBy { it.value }[i].getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        Locale.KOREAN
                    ),
                    i * 2 + 1
                )

            }
        }
    }.toList()


    WeeklyUsageChart(
        title = "TEST",
        subTitle = "",
        list = usageMap,
        convertText = { a -> "$a 개" }
    )
}
